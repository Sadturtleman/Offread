package com.android.offread.settings.domain

import com.android.offread.core.domain.navigation.AppRoutes
import com.android.offread.core.domain.navigation.NavRoute
import com.android.offread.core.domain.navigation.Page

/** S-01 설정 홈(F-028). */
object SettingsHomePage : Page {
    const val PATH = AppRoutes.SETTINGS_HOME

    override fun toRoute(): NavRoute = NavRoute(PATH)
}

/** S-03 표시 설정(F-030). */
object DisplaySettingsPage : Page {
    const val PATH = AppRoutes.SETTINGS_DISPLAY

    override fun toRoute(): NavRoute = NavRoute(PATH)
}

/** S-06 정보(F-032). */
object InfoPage : Page {
    const val PATH = AppRoutes.SETTINGS_INFO

    override fun toRoute(): NavRoute = NavRoute(PATH)
}
