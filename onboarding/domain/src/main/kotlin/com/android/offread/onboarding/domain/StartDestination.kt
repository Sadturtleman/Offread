package com.android.offread.onboarding.domain

/**
 * 스플래시(F-001)가 결정하는 앱 최초 진입 목적지.
 */
enum class StartDestination {
    /** 온보딩 미완료 → O-02 온보딩 인트로. */
    ONBOARDING,

    /** 온보딩 완료 → L-01 라이브러리. */
    LIBRARY,
}
