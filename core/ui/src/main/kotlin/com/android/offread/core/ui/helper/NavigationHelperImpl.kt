package com.android.offread.core.ui.helper

import androidx.compose.runtime.compositionLocalOf
import com.android.offread.core.domain.helper.NavigationHelper
import com.android.offread.core.domain.navigation.NavRoute
import com.android.offread.core.domain.navigation.NavSignal
import com.android.offread.core.domain.navigation.Page
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

class NavigationHelperImpl
    @Inject
    constructor() : NavigationHelper {
        private val _navigationFlow = Channel<NavSignal>(capacity = Channel.BUFFERED)
        override val navigationFlow: Flow<NavSignal> = _navigationFlow.receiveAsFlow()

        override fun navigateTo(page: Page) {
            navigateByRoute(page.toRoute())
        }

        override fun navigateByRoute(route: NavRoute) {
            emit(NavSignal.GoToDestPage(route))
        }

        override fun navigateToBack() {
            emit(NavSignal.Back)
        }

        private fun emit(navSignal: NavSignal) {
            val result = _navigationFlow.trySend(navSignal)
            if (result.isFailure) println("NavigationHelper dropped: $navSignal")
        }
    }

val LocalNavigationHelper = compositionLocalOf<NavigationHelper> { error("No NavigationHelper provided!") }
