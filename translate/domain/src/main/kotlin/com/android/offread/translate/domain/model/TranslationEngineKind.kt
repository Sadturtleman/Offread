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
    ;

    /** 프롬프트로 용어맵을 주입할 수 있는 엔진인지. */
    val supportsGlossaryPrompt: Boolean get() = this == ON_DEVICE_LLM
}
