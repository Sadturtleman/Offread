package com.android.offread.core.entity

/**
 * 특정 [LanguagePair] 를 번역하는 온디바이스 모델 1개.
 *
 * @property id 안정적 식별자(다운로드/캐시 키). 예: "translategemma-4b-ja-ko"
 * @property displayName 사용자 표기명. 예: "TranslateGemma 4B"
 * @property sizeBytes 다운로드 크기(바이트)
 * @property version 모델 버전(캐시 무효화·재검증 기준, F-021 캐시 키 구성요소)
 * @property sha256 무결성 검증용 체크섬(F-003)
 */
data class TranslationModel(
    val id: String,
    val languagePair: LanguagePair,
    val displayName: String,
    val sizeBytes: Long,
    val version: String,
    val sha256: String,
)
