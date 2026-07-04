package com.android.offread.reader.domain

import com.android.offread.core.domain.navigation.AppRoutes
import com.android.offread.core.domain.navigation.NavRoute
import com.android.offread.core.domain.navigation.Page

/** R-01 웹소설 리더(F-015). */
data class ReaderPage(
    val itemId: String,
    val chapterIndex: Int,
) : Page {
    override fun toRoute(): NavRoute = NavRoute(PATH, mapOf(ARG_ITEM_ID to itemId, ARG_CHAPTER to chapterIndex.toString()))

    companion object {
        const val PATH = AppRoutes.READER
        const val ARG_ITEM_ID = AppRoutes.ARG_ITEM_ID
        const val ARG_CHAPTER = AppRoutes.ARG_CHAPTER
    }
}
