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
    ;

    /** 캐시가 있어 네트워크 없이 열람할 수 있는 상태인지. */
    val isOfflineReady: Boolean get() = this == CACHED

    companion object {
        /**
         * 진행 수치로부터 배지를 정하는 단일 규칙(F-019). 라이브러리·상세·리더가 모두 이 규칙을 쓴다.
         *
         * @param total 대상 단위 수(챕터의 세그먼트 수, 작품의 화 수 등)
         * @param translated 그중 번역이 끝나 캐시된 수
         * @param cloudFallback 클라우드 폴백으로 번역된 단위가 있는지(논문, Phase 2)
         */
        fun of(
            total: Int,
            translated: Int,
            cloudFallback: Boolean = false,
        ): TranslationStatus =
            when {
                cloudFallback -> CLOUD_FALLBACK
                total > 0 && translated >= total -> CACHED
                translated > 0 -> TRANSLATING
                else -> UNTRANSLATED
            }
    }
}
