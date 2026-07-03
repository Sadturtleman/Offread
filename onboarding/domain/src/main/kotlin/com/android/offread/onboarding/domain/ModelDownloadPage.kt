package com.android.offread.onboarding.domain

import com.android.offread.core.domain.navigation.AppRoutes
import com.android.offread.core.domain.navigation.NavRoute
import com.android.offread.core.domain.navigation.Page

/** O-03 번역 모델 다운로드(F-003). */
object ModelDownloadPage : Page {
    const val PATH = AppRoutes.MODEL_DOWNLOAD

    override fun toRoute(): NavRoute = NavRoute(PATH)
}
