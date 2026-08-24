package com.android.offread.onboarding.data

import com.android.offread.core.entity.Language
import com.android.offread.core.entity.LanguagePair
import com.android.offread.onboarding.domain.TranslationEngine
import javax.inject.Inject
import com.android.offread.translate.domain.TranslationEngine as CoreTranslationEngine

/**
 * 온보딩 첫 번역 체험(F-004) 어댑터. 샘플 원문은 내장이지만 번역은 **실제 엔진**을 거친다 —
 * 사용자가 첫 화면에서 보는 번역이 실제 성능과 같아야 체험의 의미가 있다.
 */
class TranslationEngineImpl
    @Inject
    constructor(
        private val engine: CoreTranslationEngine,
    ) : TranslationEngine {
        override fun sampleText(pair: LanguagePair): String =
            when (pair.source) {
                Language.JAPANESE -> "ソフィアは静かに本を閉じ、窓の外の雨を眺めた。"
                Language.CHINESE -> "索菲亚静静地合上书，望着窗外的雨。"
                else -> "Sophia quietly closed the book and gazed at the rain outside the window."
            }

        override suspend fun translate(
            text: String,
            pair: LanguagePair,
        ): String = engine.translate(text, pair, glossary = emptyList())
    }
