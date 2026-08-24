package com.android.offread.translate.domain

import com.android.offread.core.entity.LanguagePair
import com.android.offread.translate.domain.model.GlossaryEntry

/**
 * 온디바이스 추론 포트(F-020). 구현은 MediaPipe LLM Inference / llama.cpp 어댑터로 교체된다.
 *
 * NOTE: 온보딩의 샘플 번역 포트(onboarding:domain)는 첫 번역 체험(F-004) 전용이며,
 * 실제 엔진이 붙을 때 이 포트로 흡수한다.
 */
interface TranslationEngine {
    /** 세그먼트 1개를 번역한다. [glossary] 는 프롬프트에 주입된다. */
    suspend fun translate(
        text: String,
        pair: LanguagePair,
        glossary: List<GlossaryEntry>,
    ): String

    /** 현재 사용 중인 모델 버전(F-021 캐시 키 구성요소). */
    suspend fun modelVersion(pair: LanguagePair): String
}

/**
 * 컬렉션 용어맵 조회 포트(F-020 2단계). 어댑터가 terms 모듈에서 확정 용어를 읽어 온다.
 */
interface GlossaryProvider {
    suspend fun glossaryFor(collectionId: String): List<GlossaryEntry>
}
