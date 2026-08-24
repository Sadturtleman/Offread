package com.android.offread.translate.data

import android.content.Context
import com.android.offread.core.entity.LanguagePair
import com.android.offread.translate.domain.TranslationEngine
import com.android.offread.translate.domain.TranslationPromptBuilder
import com.android.offread.translate.domain.model.GlossaryEntry
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 온디바이스 LLM 번역 어댑터(F-020, MediaPipe LLM Inference).
 *
 * 앱 전용 저장소의 [MODEL_DIR] 에 있는 `.task`/`.litertlm` 모델을 쓴다. 모델 파일은 라이선스
 * 동의가 필요한 배포물이라 앱이 임의로 받아오지 않는다 — 배포 경로가 정해지면(#32) 그때
 * 다운로드가 이 위치를 채우고, 그전까지는 파일을 직접 넣어 시험한다.
 *
 * 모델이 없으면 명확한 예외를 던진다. 파이프라인은 세그먼트 하나의 실패를 격리하므로
 * 화면에는 원문 + 재시도가 노출된다(P-08).
 *
 * NOTE: MediaPipe LLM Inference API 는 유지보수 모드이고 후속은 LiteRT-LM 이다.
 * 추론 호출을 이 클래스 하나로 좁혀 뒀으니 런타임 교체는 여기만 손대면 된다.
 */
@Singleton
class LlmTranslationEngine
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val promptBuilder: TranslationPromptBuilder,
    ) : TranslationEngine {
        private val mutex = Mutex()
        private var loaded: LoadedModel? = null

        override suspend fun translate(
            text: String,
            pair: LanguagePair,
            glossary: List<GlossaryEntry>,
        ): String {
            val model = loadedModel()
            val prompt = promptBuilder.build(text, pair, glossary)
            // 추론은 무겁고 세션이 하나뿐이라 직렬화한다.
            val raw =
                mutex.withLock {
                    withContext(Dispatchers.Default) { model.inference.generateResponse(prompt) }
                }
            return promptBuilder.clean(raw)
        }

        /** 캐시 키 구성요소(F-021). 모델 파일이 바뀌면 옛 번역을 재사용하지 않는다. */
        override suspend fun modelVersion(pair: LanguagePair): String {
            val file = modelFile() ?: return "llm-none"
            return "llm-${file.name}-${file.length()}"
        }

        private suspend fun loadedModel(): LoadedModel =
            mutex.withLock {
                val file = modelFile() ?: throw MissingLlmModelException(modelDir().absolutePath)
                loaded?.takeIf { it.path == file.absolutePath && it.size == file.length() }?.let { return it }
                loaded?.inference?.close()
                val inference =
                    withContext(Dispatchers.Default) {
                        LlmInference.createFromOptions(
                            context,
                            LlmInferenceOptions
                                .builder()
                                .setModelPath(file.absolutePath)
                                .setMaxTokens(MAX_TOKENS)
                                .build(),
                        )
                    }
                LoadedModel(inference, file.absolutePath, file.length()).also { loaded = it }
            }

        private fun modelDir(): File = File(context.filesDir, MODEL_DIR)

        private fun modelFile(): File? =
            modelDir()
                .listFiles { file -> file.isFile && MODEL_EXTENSIONS.any { file.name.endsWith(it) } }
                ?.maxByOrNull { it.lastModified() }

        private data class LoadedModel(
            val inference: LlmInference,
            val path: String,
            val size: Long,
        )

        private companion object {
            const val MODEL_DIR = "llm"
            val MODEL_EXTENSIONS = listOf(".task", ".litertlm")

            /** 웹소설 한 문단 + 용어 프롬프트를 감당할 정도. 모델 메모리와 직결된다. */
            const val MAX_TOKENS = 2048
        }
    }

/** 온디바이스 LLM 모델 파일이 없을 때. */
class MissingLlmModelException(
    modelDirPath: String,
) : IllegalStateException("번역 모델 파일이 없어요. $modelDirPath 에 .task 또는 .litertlm 파일을 넣어 주세요.")
