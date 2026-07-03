package com.android.offread.onboarding.domain.model

import com.android.offread.core.entity.LanguagePair

/** 첫 번역 체험(F-004)에 쓰는 내장 샘플 원문 1건. */
data class TranslationSample(
    val languagePair: LanguagePair,
    val originalText: String,
)
