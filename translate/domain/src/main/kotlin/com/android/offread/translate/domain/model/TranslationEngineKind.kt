package com.android.offread.translate.domain.model

/** 어떤 엔진으로 번역할지. 설정 시트에서 고른다. */
enum class TranslationEngineKind {
    /**
     * ML Kit 온디바이스 번역. 언어쌍당 ~30MB 모델을 SDK 가 알아서 받고 관리한다.
     * 가볍고 빠르지만 번역 품질은 전용 LLM 보다 낮다.
     */
    ML_KIT,

    /**
     * TranslateGemma 4B(LiteRT-LM). 번역 전용으로 학습돼 품질이 가장 좋다.
     * 모델 파일(~2GB, RAM 6GB+)을 직접 가져와야 한다.
     */
    TRANSLATE_GEMMA,
    ;

    /** 사용자가 모델 파일을 직접 넣어야 하는 엔진인지. */
    val requiresModelFile: Boolean get() = this == TRANSLATE_GEMMA
}
