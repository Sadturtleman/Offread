package com.android.offread.core.entity

/**
 * 아이템·챕터의 번역 상태 배지(F-019 전역 배지 시스템). 라이브러리·상세·리더에서 동일 규칙으로 표시한다.
 */
enum class TranslationStatus {
    /** 아직 번역 안 됨. */
    UNTRANSLATED,

    /** 번역 진행 중. */
    TRANSLATING,

    /** 캐시됨 — 완전 오프라인 열람 가능. */
    CACHED,

    /** 클라우드 폴백(논문, opt-in) 사용. */
    CLOUD_FALLBACK,
}
