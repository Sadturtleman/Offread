package com.android.offread

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation3.runtime.NavKey
import com.android.offread.core.domain.helper.MessageHelper
import com.android.offread.core.domain.helper.NavigationHelper
import com.android.offread.core.domain.navigation.HomePage
import com.android.offread.core.ui.helper.LocalMessageHelper
import com.android.offread.core.ui.helper.LocalNavigationHelper
import com.android.offread.navigation.GenericNavKey
import com.android.offread.navigation.RootComposable

/**
 * 앱 루트 컴포저블. Hilt 가 주입한 헬퍼들을 CompositionLocal 로 제공하고 [RootComposable] 을 그린다.
 * @AndroidEntryPoint MainActivity 가 헬퍼를 주입받아 호출한다. ViewModel 은 hiltViewModel() 이 직접 해결한다.
 */
@Composable
fun AppRoot(
    navigationHelper: NavigationHelper,
    messageHelper: MessageHelper,
    startStack: List<NavKey> = listOf(GenericNavKey(HomePage.PATH)),
) {
    CompositionLocalProvider(
        LocalNavigationHelper provides navigationHelper,
        LocalMessageHelper provides messageHelper,
    ) {
        RootComposable(startStack = startStack)
    }
}
