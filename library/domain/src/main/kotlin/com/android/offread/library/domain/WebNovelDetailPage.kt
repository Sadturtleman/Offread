package com.android.offread.library.domain

import com.android.offread.core.domain.navigation.AppRoutes
import com.android.offread.core.domain.navigation.NavRoute
import com.android.offread.core.domain.navigation.Page

/** L-03 웹소설 아이템 상세(F-008). */
data class WebNovelDetailPage(
    val itemId: String,
) : Page {
    override fun toRoute(): NavRoute = NavRoute(PATH, mapOf(ARG_ITEM_ID to itemId))

    companion object {
        const val PATH = AppRoutes.WEBNOVEL_DETAIL
        const val ARG_ITEM_ID = "itemId"
    }
}
