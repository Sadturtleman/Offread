package com.android.offread.onboarding.domain

import com.android.offread.core.domain.navigation.AppRoutes
import com.android.offread.core.domain.navigation.NavRoute
import com.android.offread.core.domain.navigation.Page

/** O-01 스플래시. 앱 최초 진입점(F-001). */
object SplashPage : Page {
    const val PATH = AppRoutes.SPLASH

    override fun toRoute(): NavRoute = NavRoute(PATH)
}
