package com.android.offread.core.entity

/**
 * 번역이 지원하는 언어. 온디바이스 번역 모델의 소스/타깃 축이 된다.
 *
 * @property code ISO 639-1 코드. 번역 모델 프롬프트가 이 코드를 요구한다(예: TranslateGemma).
 */
enum class Language(
    val code: String,
) {
    KOREAN("ko"),
    JAPANESE("ja"),
    CHINESE("zh"),
    ENGLISH("en"),
}
