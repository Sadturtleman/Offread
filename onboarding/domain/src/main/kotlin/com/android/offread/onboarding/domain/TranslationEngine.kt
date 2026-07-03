package com.android.offread.onboarding.domain

import com.android.offread.core.entity.LanguagePair

/**
 * 온디바이스 번역 엔진 포트(헥사고날). 실제 추론(MediaPipe LLM Inference/llama.cpp, F-020)은
 * 어댑터(onboarding:data)에 감춘다. 온보딩 첫 번역 체험(F-004)에서 사용한다.
 */
interface TranslationEngine {
    /** 언어쌍별 내장 샘플 원문. */
    fun sampleText(pair: LanguagePair): String

    /** [text] 를 [pair] 방향으로 온디바이스 번역한다. */
    suspend fun translate(
        text: String,
        pair: LanguagePair,
    ): String
}
