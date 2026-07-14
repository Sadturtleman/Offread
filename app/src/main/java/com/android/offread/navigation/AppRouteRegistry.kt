package com.android.offread.navigation

import com.android.offread.core.domain.navigation.HomePage
import com.android.offread.importer.domain.ImportSheetPage
import com.android.offread.importer.domain.WebNovelImportPage
import com.android.offread.importer.presentation.sheet.ImportSheetScreen
import com.android.offread.importer.presentation.webnovel.WebNovelImportScreen
import com.android.offread.library.domain.WebNovelDetailPage
import com.android.offread.library.presentation.LibraryScreen
import com.android.offread.library.presentation.detail.WebNovelDetailScreen
import com.android.offread.onboarding.domain.FirstTranslationPage
import com.android.offread.onboarding.domain.ModelDownloadPage
import com.android.offread.onboarding.domain.OnboardingIntroPage
import com.android.offread.onboarding.domain.SplashPage
import com.android.offread.onboarding.presentation.download.ModelDownloadScreen
import com.android.offread.onboarding.presentation.firsttranslation.FirstTranslationScreen
import com.android.offread.onboarding.presentation.intro.OnboardingIntroScreen
import com.android.offread.onboarding.presentation.splash.SplashScreen
import com.android.offread.reader.domain.ReaderPage
import com.android.offread.reader.presentation.ReaderScreen
import com.android.offread.settings.domain.DisplaySettingsPage
import com.android.offread.settings.domain.InfoPage
import com.android.offread.settings.domain.SettingsHomePage
import com.android.offread.settings.presentation.display.DisplaySettingsScreen
import com.android.offread.settings.presentation.home.SettingsHomeScreen
import com.android.offread.settings.presentation.info.InfoScreen
import com.android.offread.terms.domain.TermMapPage
import com.android.offread.terms.presentation.TermMapScreen

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
        AppRoute(
            path = ImportSheetPage.PATH,
            render = { ImportSheetScreen() },
        ),
        AppRoute(
            path = WebNovelImportPage.PATH,
            render = { WebNovelImportScreen() },
        ),
        AppRoute(
            path = WebNovelDetailPage.PATH,
            render = { args -> WebNovelDetailScreen(itemId = args[WebNovelDetailPage.ARG_ITEM_ID].orEmpty()) },
        ),
        AppRoute(
            path = ReaderPage.PATH,
            render = { args ->
                ReaderScreen(
                    itemId = args[ReaderPage.ARG_ITEM_ID].orEmpty(),
                    chapterIndex = args[ReaderPage.ARG_CHAPTER]?.toIntOrNull() ?: 1,
                )
            },
        ),
        AppRoute(
            path = TermMapPage.PATH,
            render = { args -> TermMapScreen(collectionId = args[TermMapPage.ARG_COLLECTION_ID].orEmpty()) },
        ),
        AppRoute(
            path = SettingsHomePage.PATH,
            render = { SettingsHomeScreen() },
        ),
        AppRoute(
            path = DisplaySettingsPage.PATH,
            render = { DisplaySettingsScreen() },
        ),
        AppRoute(
            path = InfoPage.PATH,
            render = { InfoScreen() },
        ),
    )

val appRouteByPath: Map<String, AppRoute> = appRoutes.associateBy { it.path }

val bottomTabRoutes: List<AppRoute> = appRoutes.filter { it.isBottomTab }
