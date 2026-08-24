package com.android.offread.translate.domain

/** 온디바이스 LLM 모델 파일이 어느 런타임용인지. 확장자로 갈린다. */
enum class LlmRuntime {
    /** MediaPipe LLM Inference — `.task` */
    MEDIAPIPE_TASK,

    /** LiteRT-LM — `.litertlm` (TranslateGemma 4B 번들이 이 형식) */
    LITERT_LM,
}

/**
 * 기기에 설치된 모델 파일 1개.
 *
 * @property name 파일명(식별자 겸용)
 */
data class LlmModelFile(
    val name: String,
    val sizeBytes: Long,
    val runtime: LlmRuntime,
)

/**
 * 모델 파일 보관 포트(F-020).
 *
 * TranslateGemma 등 Gemma 계열 가중치는 라이선스 동의가 필요한 gated 배포물이라 앱이 임의로
 * 내려받지 않는다. 사용자가 받아 둔 파일을 앱 전용 저장소로 **가져오는** 방식만 제공한다.
 */
interface LlmModelStore {
    suspend fun installed(): List<LlmModelFile>

    /**
     * 파일을 앱 저장소로 복사한다. 파일명은 어댑터가 문서 메타에서 읽는다.
     *
     * @param uri 플랫폼 문서 식별자(안드로이드 SAF 의 Uri 문자열). 도메인은 문자열로만 다룬다.
     */
    suspend fun import(uri: String): LlmModelFile

    suspend fun delete(name: String)
}
