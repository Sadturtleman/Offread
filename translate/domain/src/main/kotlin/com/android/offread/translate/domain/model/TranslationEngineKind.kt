package com.android.offread.translate.domain.model

/** 어떤 엔진으로 번역할지. 설정 시트에서 고른다. */
enum class TranslationEngineKind {
    /**
     * TranslateGemma 4B(LiteRT-LM). 번역 전용으로 학습돼 품질이 가장 좋다. **기본값.**
     * 모델 파일(~2GB, RAM 6GB+)을 직접 가져와야 하고, 없으면 번역이 되지 않는다.
     */
    TRANSLATE_GEMMA,

    /**
     * ML Kit 온디바이스 번역. 언어쌍당 ~30MB 모델을 SDK 가 알아서 받고 관리한다.
     * 가볍고 빠르지만 번역 품질이 실사용에 못 미쳐 기본에서 내렸다 — 모델 파일이
     * 없거나 기기가 버거울 때의 대안이다.
     */
    ML_KIT,
    ;

    /** 사용자가 모델 파일을 직접 넣어야 하는 엔진인지. */
    val requiresModelFile: Boolean get() = this == TRANSLATE_GEMMA
}
