package com.android.offread.translate.data

import com.android.offread.core.entity.LanguagePair
import com.android.offread.translate.domain.TranslationEngine
import com.android.offread.translate.domain.TranslationEnginePreference
import com.android.offread.translate.domain.model.TranslationEngineKind
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 설정에서 고른 엔진으로 번역을 넘긴다(F-020). 매 호출마다 선택을 읽으므로 설정을 바꾸면
 * 다음 번역부터 바로 반영된다. 엔진마다 modelVersion 이 달라 캐시가 섞이지 않는다(F-021).
 */
@Singleton
class SwitchingTranslationEngine
    @Inject
    constructor(
        private val preference: TranslationEnginePreference,
        @MlKitEngine private val mlKit: TranslationEngine,
        @TranslateGemmaEngine private val translateGemma: TranslationEngine,
    ) : TranslationEngine {
        override suspend fun translate(
            text: String,
            pair: LanguagePair,
        ): String = current().translate(text, pair)

        override suspend fun modelVersion(pair: LanguagePair): String = current().modelVersion(pair)

        private suspend fun current(): TranslationEngine =
            when (preference.selected.first()) {
                TranslationEngineKind.ML_KIT -> mlKit
                TranslationEngineKind.TRANSLATE_GEMMA -> translateGemma
            }
    }
