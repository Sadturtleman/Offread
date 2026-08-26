package com.android.offread.translate.data

import android.content.Context
import com.android.offread.core.entity.LanguagePair
import com.android.offread.translate.domain.TranslateGemmaPromptBuilder
import com.android.offread.translate.domain.TranslationEngine
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TranslateGemma 4B 어댑터(F-020, LiteRT-LM 런타임).
 *
 * 번역 전용으로 학습된 모델이라 품질이 가장 좋다. 대신 제약이 붙는다.
 * - 프롬프트가 `<src>/<dst>/<text>` 태그 형식으로 고정돼 있다.
 * - 커뮤니티 변환 번들 기준 컨텍스트가 1024 토큰이라 세그먼트가 길면 잘릴 수 있다.
 *   파이프라인의 문단 분할(기본 400자)이 이 범위 안에 들어온다.
 * - 백엔드는 CPU 고정 — 현재 이 번들의 GPU 초기화는 실패한다(모델 카드 명시).
 * - 엔진 초기화가 10초 안팎 걸려 첫 번역이 느리다. 한 번 만들어 재사용한다.
 */
@Singleton
class LiteRtLmTranslationEngine
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val promptBuilder: TranslateGemmaPromptBuilder,
    ) : TranslationEngine {
        private val mutex = Mutex()
        private var loaded: LoadedEngine? = null

        override suspend fun translate(
            text: String,
            pair: LanguagePair,
        ): String {
            val engine = loadedEngine()
            val prompt = promptBuilder.build(text, pair)
            val answer =
                mutex.withLock {
                    withContext(Dispatchers.Default) {
                        engine.createConversation().use { conversation -> conversation.sendMessage(prompt) }
                    }
                }
            return promptBuilder.clean(answer.text())
        }

        /** 캐시 키 구성요소(F-021). 모델 파일이 바뀌면 옛 번역을 재사용하지 않는다. */
        override suspend fun modelVersion(pair: LanguagePair): String {
            val file = modelFile() ?: return "litertlm-none"
            return "litertlm-${file.name}-${file.length()}"
        }

        private suspend fun loadedEngine(): Engine =
            mutex.withLock {
                val file = modelFile() ?: throw MissingLlmModelException
                loaded?.takeIf { it.path == file.absolutePath && it.size == file.length() }?.let { return it.engine }
                loaded?.engine?.close()
                val engine =
                    withContext(Dispatchers.Default) {
                        Engine(
                            EngineConfig(
                                modelPath = file.absolutePath,
                                backend = Backend.CPU(),
                                maxNumTokens = MAX_NUM_TOKENS,
                            ),
                        ).apply { initialize() }
                    }
                LoadedEngine(engine, file.absolutePath, file.length()).also { loaded = it }.engine
            }

        private fun modelFile(): File? = LlmModelDirectory(File(context.filesDir, LlmModelDirectory.DIR_NAME)).latest()

        /** 응답에서 텍스트 조각만 이어 붙인다(이미지·툴 응답은 번역에 쓰지 않는다). */
        private fun Message.text(): String =
            contents.contents
                .filterIsInstance<Content.Text>()
                .joinToString(separator = "") { it.text }

        private data class LoadedEngine(
            val engine: Engine,
            val path: String,
            val size: Long,
        )

        private companion object {
            /** 커뮤니티 변환 번들의 prefill·KV 캐시가 1024 토큰이다. 그 이상을 요구하면 초기화가 실패한다. */
            const val MAX_NUM_TOKENS = 1024
        }
    }

/** TranslateGemma 모델 파일이 없을 때. */
object MissingLlmModelException :
    IllegalStateException("TranslateGemma 모델 파일이 없어요. 설정에서 .litertlm 모델을 가져와 주세요.")
