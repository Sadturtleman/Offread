package com.android.offread.onboarding.data

import com.android.offread.core.entity.Language
import com.android.offread.core.entity.LanguagePair
import com.android.offread.onboarding.domain.TranslationEngine
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * [TranslationEngine] 스텁 어댑터. 온보딩 첫 번역 체험(F-004)을 구동하기 위한 내장 샘플·캔드 번역만 제공한다.
 * 실제 온디바이스 추론(MediaPipe LLM Inference/llama.cpp, F-020)은 후속 인프라 태스크에서 이 클래스를 교체한다.
 */
class TranslationEngineImpl
    @Inject
    constructor() : TranslationEngine {
        override fun sampleText(pair: LanguagePair): String =
            when (pair.source) {
                Language.JAPANESE -> "ソフィアは静かに本を閉じ、窓の外の雨を眺めた。"
                Language.CHINESE -> "索菲亚静静地合上书，望着窗外的雨。"
                else -> "Sophia quietly closed the book and gazed at the rain outside the window."
            }

        override suspend fun translate(
            text: String,
            pair: LanguagePair,
        ): String {
            // 온디바이스 추론 지연을 흉내낸다(스텁).
            delay(STUB_LATENCY_MILLIS)
            return "소피아는 조용히 책을 덮고, 창밖의 비를 바라보았다."
        }

        private companion object {
            const val STUB_LATENCY_MILLIS = 400L
        }
    }
