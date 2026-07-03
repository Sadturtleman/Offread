package com.android.offread.navigation

import com.android.offread.core.domain.navigation.HomePage
import com.android.offread.library.presentation.LibraryScreen
import com.android.offread.onboarding.domain.FirstTranslationPage
import com.android.offread.onboarding.domain.ModelDownloadPage
import com.android.offread.onboarding.domain.OnboardingIntroPage
import com.android.offread.onboarding.domain.SplashPage
import com.android.offread.onboarding.presentation.download.ModelDownloadScreen
import com.android.offread.onboarding.presentation.firsttranslation.FirstTranslationScreen
import com.android.offread.onboarding.presentation.intro.OnboardingIntroScreen
import com.android.offread.onboarding.presentation.splash.SplashScreen

/**
 * 앱의 모든 페이지 메타데이터 + 렌더러 모음.
 * 새 화면 추가 시 본 리스트에 한 줄을 더한다.
 */
val appRoutes: List<AppRoute> =
    listOf(
        AppRoute(
            path = SplashPage.PATH,
            isRoot = true,
            render = { SplashScreen() },
        ),
        AppRoute(
            path = OnboardingIntroPage.PATH,
            isRoot = true,
            render = { OnboardingIntroScreen() },
        ),
        AppRoute(
            path = ModelDownloadPage.PATH,
            render = { ModelDownloadScreen() },
        ),
        AppRoute(
            path = FirstTranslationPage.PATH,
            render = { FirstTranslationScreen() },
        ),
        AppRoute(
            path = HomePage.PATH,
            isRoot = true,
            render = { LibraryScreen() },
        ),
    )

val appRouteByPath: Map<String, AppRoute> = appRoutes.associateBy { it.path }

val bottomTabRoutes: List<AppRoute> = appRoutes.filter { it.isBottomTab }
