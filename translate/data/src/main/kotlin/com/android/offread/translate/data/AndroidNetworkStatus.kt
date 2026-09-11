package com.android.offread.translate.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import com.android.offread.translate.domain.NetworkStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** 종량제가 아닌 망(대개 Wi-Fi)에서만 2GB 자동 다운로드를 켠다. */
@Singleton
class AndroidNetworkStatus
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : NetworkStatus {
        override suspend fun isUnmetered(): Boolean {
            val manager = context.getSystemService<ConnectivityManager>() ?: return false
            val capabilities = manager.getNetworkCapabilities(manager.activeNetwork) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    }
