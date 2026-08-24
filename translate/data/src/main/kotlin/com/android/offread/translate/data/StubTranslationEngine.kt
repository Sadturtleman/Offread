package com.android.offread.translate.data

import com.android.offread.core.entity.LanguagePair
import com.android.offread.translate.domain.TranslationEngine
import com.android.offread.translate.domain.model.GlossaryEntry
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * [TranslationEngine] 스텁 어댑터. 파이프라인(F-020)·캐시(F-021)를 실제로 구동하기 위한 자리이며,
 * 실제 온디바이스 추론(MediaPipe LLM Inference / llama.cpp, 모델 TranslateGemma·NLLB·Sugoi)은
 * 이 클래스만 교체하면 된다. 용어 주입·후처리·캐시 키 계산은 전부 도메인에서 이미 실제로 돈다.
 */
class StubTranslationEngine
    @Inject
    constructor() : TranslationEngine {
        override suspend fun translate(
            text: String,
            pair: LanguagePair,
            glossary: List<GlossaryEntry>,
        ): String {
            // 온디바이스 추론 지연을 흉내낸다(스텁).
            delay(STUB_LATENCY_MILLIS)
            return "[${pair.source.name.take(2)}→${pair.target.name.take(2)}] $text"
        }

        /** 모델 교체 시 이 값이 바뀌면 옛 캐시가 자동으로 무효화된다(F-021). */
        override suspend fun modelVersion(pair: LanguagePair): String = "stub-1-${pair.name}"

        private companion object {
            const val STUB_LATENCY_MILLIS = 120L
        }
    }
