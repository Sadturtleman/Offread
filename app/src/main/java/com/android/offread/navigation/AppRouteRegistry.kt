package com.android.offread.navigation

import com.android.offread.core.domain.navigation.TranslatePage
import com.android.offread.translate.presentation.TranslateScreen

/**
 * 앱의 모든 페이지 메타데이터 + 렌더러 모음. MVP 는 번역 화면 하나뿐이다.
 */
val appRoutes: List<AppRoute> =
    listOf(
        AppRoute(
            path = TranslatePage.PATH,
            isRoot = true,
            render = { TranslateScreen() },
        ),
    )

val appRouteByPath: Map<String, AppRoute> = appRoutes.associateBy { it.path }

val bottomTabRoutes: List<AppRoute> = appRoutes.filter { it.isBottomTab }
