package com.android.offread.translate.data

import com.android.offread.core.entity.Language
import com.android.offread.core.entity.LanguagePair
import com.android.offread.translate.domain.TranslationEngine
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * ML Kit 온디바이스 번역 어댑터(F-020).
 *
 * 언어쌍당 ~30MB 모델을 SDK 가 직접 받고 관리하므로 모델 배포 경로가 없어도 바로 번역이 된다.
 * 가볍고 빠르지만 번역 품질은 전용 LLM(TranslateGemma)보다 낮다.
 *
 * 모델 다운로드는 셀룰러도 허용한다. P-05 의 'Wi-Fi 기본'은 수 GB 짜리 LLM 모델을 겨냥한
 * 것이고, 여기 모델은 언어쌍당 ~30MB 로 원문 수집(셀룰러 허용)과 같은 규모다. Wi-Fi 를 강제하면
 * 셀룰러에서 첫 번역이 조건 충족까지 무한정 대기하게 되어 리더가 멈춘 것처럼 보인다.
 */
@Singleton
class MlKitTranslationEngine
    @Inject
    constructor() : TranslationEngine {
        private val translators = mutableMapOf<LanguagePair, Translator>()
        private val readyPairs = mutableSetOf<LanguagePair>()
        private val mutex = Mutex()

        override suspend fun translate(
            text: String,
            pair: LanguagePair,
        ): String {
            val translator = translatorFor(pair)
            return translator.translate(text).await()
        }

        /** 모델을 새로 받으면 번역이 달라질 수 있으나, ML Kit 은 버전을 노출하지 않아 SDK 버전을 쓴다. */
        override suspend fun modelVersion(pair: LanguagePair): String = "mlkit-$MLKIT_MODEL_REVISION-${pair.name}"

        private suspend fun translatorFor(pair: LanguagePair): Translator =
            mutex.withLock {
                val translator =
                    translators.getOrPut(pair) {
                        Translation.getClient(
                            TranslatorOptions
                                .Builder()
                                .setSourceLanguage(pair.source.toMlKit())
                                .setTargetLanguage(pair.target.toMlKit())
                                .build(),
                        )
                    }
                if (pair !in readyPairs) {
                    translator.downloadModelIfNeeded(DownloadConditions.Builder().build()).await()
                    readyPairs += pair
                }
                translator
            }

        private fun Language.toMlKit(): String =
            when (this) {
                Language.KOREAN -> TranslateLanguage.KOREAN
                Language.JAPANESE -> TranslateLanguage.JAPANESE
                Language.CHINESE -> TranslateLanguage.CHINESE
                Language.ENGLISH -> TranslateLanguage.ENGLISH
            }

        private companion object {
            /** 캐시 키에 들어가는 값. ML Kit 모델을 갈아끼운 셈이 되면 이 값을 올린다. */
            const val MLKIT_MODEL_REVISION = "1"
        }
    }

/** Play Services [Task] 를 코루틴으로 잇는다(추가 의존성 없이 최소 구현). */
private suspend fun <T> Task<T>.await(): T =
    suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result -> continuation.resume(result) }
        addOnFailureListener { error -> continuation.resumeWithException(error) }
        addOnCanceledListener { continuation.cancel() }
    }
