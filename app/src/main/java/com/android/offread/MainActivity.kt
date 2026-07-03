package com.android.offread

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.android.offread.core.domain.helper.MessageHelper
import com.android.offread.core.domain.helper.NavigationHelper
import com.android.offread.deeplink.resolveNewIntentRoute
import com.android.offread.deeplink.resolveStartStack
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var navigationHelper: NavigationHelper

    @Inject
    lateinit var messageHelper: MessageHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        val startStack = resolveStartStack(intent?.data)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppRoot(
                navigationHelper = navigationHelper,
                messageHelper = messageHelper,
                startStack = startStack,
            )
        }
    }

    // 앱이 떠 있는 동안 들어온 딥링크(offread://app/...) 처리. 미등록 path 는 무시된다.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data
            ?.let { resolveNewIntentRoute(it) }
            ?.let { navigationHelper.navigateByRoute(it) }
    }
}
