package com.android.offread.settings.domain.model

/** 리더 테마(S-03). */
enum class DisplayTheme {
    LIGHT,
    DARK,
    SEPIA,
}

/** 기본 번역 표시 방식(S-03, 주로 논문 리더). */
enum class TranslationDisplayMode {
    /** 번역만. */
    TRANSLATED_ONLY,

    /** 원문·번역 병행. */
    BILINGUAL,
}

/**
 * 표시 설정(S-03). 리더 기본값으로 사용되며 온디바이스 로컬 저장(P-01).
 */
data class DisplaySettings(
    val fontScale: Float = 1.0f,
    val theme: DisplayTheme = DisplayTheme.LIGHT,
    val translationDisplay: TranslationDisplayMode = TranslationDisplayMode.TRANSLATED_ONLY,
) {
    companion object {
        const val MIN_FONT_SCALE = 0.8f
        const val MAX_FONT_SCALE = 1.6f
    }
}
