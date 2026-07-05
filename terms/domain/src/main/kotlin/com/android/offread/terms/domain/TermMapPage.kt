package com.android.offread.terms.domain

import com.android.offread.core.domain.navigation.AppRoutes
import com.android.offread.core.domain.navigation.NavRoute
import com.android.offread.core.domain.navigation.Page

/** T-01 용어맵(컬렉션 스코프, F-025). */
data class TermMapPage(
    val collectionId: String,
) : Page {
    override fun toRoute(): NavRoute = NavRoute(PATH, mapOf(ARG_COLLECTION_ID to collectionId))

    companion object {
        const val PATH = AppRoutes.TERM_MAP
        const val ARG_COLLECTION_ID = AppRoutes.ARG_COLLECTION_ID
    }
}
