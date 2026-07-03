package com.android.offread.core.entity

/**
 * 번역 방향(소스 → 타깃) 단위. 온디바이스 번역 모델 1개에 대응한다.
 *
 * MVP(Phase 1)는 웹소설 대상 [JA_KO], [ZH_KO] 만 [Availability.AVAILABLE].
 * [EN_KO] 는 논문(Phase 2)과 함께 활성화되므로 [Availability.COMING_SOON].
 *
 * @property modelSizeBytes 다운로드할 번역 모델의 대략 크기(바이트). UI 에서 "약 X.XGB" 로 표기.
 */
enum class LanguagePair(
    val source: Language,
    val target: Language,
    val modelSizeBytes: Long,
    val availability: Availability,
) {
    JA_KO(Language.JAPANESE, Language.KOREAN, 2_100L * 1024 * 1024, Availability.AVAILABLE),
    ZH_KO(Language.CHINESE, Language.KOREAN, 2_300L * 1024 * 1024, Availability.AVAILABLE),
    EN_KO(Language.ENGLISH, Language.KOREAN, 0L, Availability.COMING_SOON),
    ;

    val isSelectable: Boolean get() = availability == Availability.AVAILABLE

    enum class Availability {
        /** 지금 선택·다운로드 가능. */
        AVAILABLE,

        /** 후속 Phase 제공 예정 — 화면에는 노출하되 선택 불가. */
        COMING_SOON,
    }
}
