package com.android.offread.onboarding.domain

import com.android.offread.core.domain.navigation.AppRoutes
import com.android.offread.core.domain.navigation.NavRoute
import com.android.offread.core.domain.navigation.Page

/** O-04 첫 번역 체험(F-004). */
object FirstTranslationPage : Page {
    const val PATH = AppRoutes.FIRST_TRANSLATION

    override fun toRoute(): NavRoute = NavRoute(PATH)
}
