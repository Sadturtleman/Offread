package com.android.offread.onboarding.presentation.intro

import com.android.offread.core.entity.Language
import com.android.offread.core.entity.LanguagePair

/** "일본어 → 한국어" 형식의 표시 라벨. */
internal fun LanguagePair.displayLabel(): String = "${source.displayName()} → ${target.displayName()}"

/** 카드 보조 문구: 사용 가능하면 모델 크기, 제공 예정이면 안내. */
internal fun LanguagePair.displaySubtitle(): String =
    when (availability) {
        LanguagePair.Availability.AVAILABLE -> "모델 약 ${formatModelSize(modelSizeBytes)}"
        LanguagePair.Availability.COMING_SOON -> "논문 번역과 함께 제공 예정"
    }

private fun Language.displayName(): String =
    when (this) {
        Language.KOREAN -> "한국어"
        Language.JAPANESE -> "일본어"
        Language.CHINESE -> "중국어"
        Language.ENGLISH -> "영어"
    }

/** 바이트 → "2.1GB" 형태. GB 미만은 MB 로 표기. */
private fun formatModelSize(bytes: Long): String {
    val gb = bytes.toDouble() / (1024 * 1024 * 1024)
    if (gb >= 1.0) return "%.1fGB".format(gb)
    val mb = bytes.toDouble() / (1024 * 1024)
    return "%.0fMB".format(mb)
}
