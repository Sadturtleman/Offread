package com.android.offread.core.domain.navigation

/**
 * 앱의 모든 화면 path 단일 소스(single source of truth).
 *
 * MVP 는 번역 화면 하나뿐이다.
 */
object AppRoutes {
    const val TRANSLATE = "translate"
}

/** 유일한 화면: URL 을 넣으면 그 페이지를 번역해 보여준다. */
object TranslatePage : Page {
    const val PATH = AppRoutes.TRANSLATE

    override fun toRoute(): NavRoute = NavRoute(PATH)
}
