package com.android.offread.onboarding.data

import com.android.offread.core.entity.LanguagePair
import com.android.offread.core.entity.TranslationModel

/**
 * MVP 번역 모델 카탈로그(F-020 후보 TranslateGemma 4B). 지금은 하드코딩이며,
 * 추후 원격 매니페스트/버전 관리로 대체한다. sha256 은 실제 다운로드 통합(후속) 시 채운다.
 */
internal object ModelCatalog {
    private val byPair: Map<LanguagePair, TranslationModel> =
        listOf(
            TranslationModel(
                id = "translategemma-4b-ja-ko",
                languagePair = LanguagePair.JA_KO,
                displayName = "TranslateGemma 4B",
                sizeBytes = LanguagePair.JA_KO.modelSizeBytes,
                version = "1.0",
                sha256 = "",
            ),
            TranslationModel(
                id = "translategemma-4b-zh-ko",
                languagePair = LanguagePair.ZH_KO,
                displayName = "TranslateGemma 4B",
                sizeBytes = LanguagePair.ZH_KO.modelSizeBytes,
                version = "1.0",
                sha256 = "",
            ),
        ).associateBy { it.languagePair }

    fun forPairs(pairs: Set<LanguagePair>): List<TranslationModel> = pairs.mapNotNull { byPair[it] }
}
