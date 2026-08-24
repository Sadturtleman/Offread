package com.android.offread.translate.domain.model

/**
 * 어떤 추론 엔진으로 번역할지(F-020). 설정(S-02)에서 고른다.
 */
enum class TranslationEngineKind {
    /**
     * ML Kit 온디바이스 번역. 언어쌍당 ~30MB 모델을 SDK 가 알아서 받고 관리한다.
     * LLM 이 아니라 프롬프트로 용어를 넣을 수 없어, 용어맵은 후처리 치환으로만 반영된다.
     */
    ML_KIT,

    /**
     * 온디바이스 LLM(MediaPipe LLM Inference). 기기에 넣어 둔 .task/.litertlm 모델을 쓴다.
     * 용어맵을 프롬프트로 주입할 수 있어 캐논 번역 일관성이 높다.
     */
    ON_DEVICE_LLM,

    /**
     * TranslateGemma 4B(LiteRT-LM 런타임). 번역 전용으로 학습돼 품질이 가장 좋다.
     * 프롬프트가 `<src>/<dst>/<text>` 태그 형식으로 고정돼 **용어 지시문을 넣을 자리가 없다** —
     * 용어맵은 후처리 치환으로만 보장된다. 모델 파일(~2GB, RAM 6GB+)을 직접 가져와야 한다.
     */
    TRANSLATE_GEMMA,
    ;

    /** 프롬프트로 용어맵을 주입할 수 있는 엔진인지. */
    val supportsGlossaryPrompt: Boolean get() = this == ON_DEVICE_LLM

    /** 사용자가 모델 파일을 직접 넣어야 하는 엔진인지. */
    val requiresModelFile: Boolean get() = this == ON_DEVICE_LLM || this == TRANSLATE_GEMMA
}
