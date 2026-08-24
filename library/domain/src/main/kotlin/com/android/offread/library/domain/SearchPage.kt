package com.android.offread.library.domain

import com.android.offread.core.domain.navigation.AppRoutes
import com.android.offread.core.domain.navigation.NavRoute
import com.android.offread.core.domain.navigation.Page

/** L-05 검색(F-010). [collectionId] 가 있으면 컬렉션 내 검색으로 시작한다. */
data class SearchPage(
    val collectionId: String? = null,
) : Page {
    override fun toRoute(): NavRoute = NavRoute(PATH, collectionId?.let { mapOf(ARG_COLLECTION_ID to it) } ?: emptyMap())

    companion object {
        const val PATH = AppRoutes.SEARCH
        const val ARG_COLLECTION_ID = AppRoutes.ARG_COLLECTION_ID
    }
}
