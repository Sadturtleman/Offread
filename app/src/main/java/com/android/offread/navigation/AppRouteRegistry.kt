package com.android.offread.navigation

import com.android.offread.core.domain.navigation.HomePage
import com.android.offread.home.HomeScreen

/**
 * 앱의 모든 페이지 메타데이터 + 렌더러 모음.
 * 새 화면 추가 시 본 리스트에 한 줄을 더한다.
 *
 * 지금은 스타터 홈 화면 하나만 등록되어 있다. feature 를 추가할 때마다
 * 각 feature 의 Page + Screen 을 여기에 연결한다.
 */
val appRoutes: List<AppRoute> =
    listOf(
        AppRoute(
            path = HomePage.PATH,
            isRoot = true,
            render = { HomeScreen() },
        ),
    )

val appRouteByPath: Map<String, AppRoute> = appRoutes.associateBy { it.path }

val bottomTabRoutes: List<AppRoute> = appRoutes.filter { it.isBottomTab }
