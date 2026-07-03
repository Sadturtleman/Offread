package com.android.offread.onboarding.domain

import com.android.offread.core.domain.navigation.AppRoutes
import com.android.offread.core.domain.navigation.NavRoute
import com.android.offread.core.domain.navigation.Page

/** O-02 온보딩 인트로·언어쌍 선택(F-002). */
object OnboardingIntroPage : Page {
    const val PATH = AppRoutes.ONBOARDING_INTRO

    override fun toRoute(): NavRoute = NavRoute(PATH)
}
