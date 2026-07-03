package com.android.offread.importer.domain

import com.android.offread.core.domain.navigation.AppRoutes
import com.android.offread.core.domain.navigation.NavRoute
import com.android.offread.core.domain.navigation.Page

/** I-01 가져오기 유형 선택 시트(F-011). */
object ImportSheetPage : Page {
    const val PATH = AppRoutes.IMPORT_SHEET

    override fun toRoute(): NavRoute = NavRoute(PATH)
}

/** I-02 웹소설 URL 가져오기(F-012). */
object WebNovelImportPage : Page {
    const val PATH = AppRoutes.IMPORT_WEBNOVEL

    override fun toRoute(): NavRoute = NavRoute(PATH)
}
