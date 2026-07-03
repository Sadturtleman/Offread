package com.android.offread.onboarding.domain

import com.android.offread.core.entity.LanguagePair
import kotlinx.coroutines.flow.Flow

/**
 * 온보딩 상태의 로컬 저장 포트(헥사고날). 서버 없음 — 전부 온디바이스(P-01).
 * 구현체는 어댑터 모듈(onboarding:data)에서 DataStore 로 제공한다.
 */
interface OnboardingRepository {
    /** 온보딩 플로우를 끝까지 마쳤는지. 최초 실행 판별(F-001)의 기준. */
    val isOnboardingComplete: Flow<Boolean>

    /** 사용자가 선택한 번역 언어쌍(F-002). 이후 설정(S-02)에서 변경 가능. */
    val selectedLanguagePairs: Flow<Set<LanguagePair>>

    suspend fun setSelectedLanguagePairs(pairs: Set<LanguagePair>)

    suspend fun setOnboardingComplete(complete: Boolean)
}
