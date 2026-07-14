package com.android.offread.settings.presentation.info

import com.android.offread.settings.domain.AppInfo
import com.android.offread.settings.domain.AppInfoProvider
import com.android.offread.settings.domain.usecase.GetAppInfoUseCase
import com.android.offread.settings.presentation.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class InfoViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `앱 정보를 노출한다`() {
        val provider =
            object : AppInfoProvider {
                override fun get(): AppInfo = AppInfo(versionName = "1.0", contactEmail = "dev@offread.app")
            }

        val vm = InfoViewModel(GetAppInfoUseCase(provider))

        assertEquals(
            "1.0",
            vm.uiState.value.appInfo
                ?.versionName,
        )
        assertEquals(
            "dev@offread.app",
            vm.uiState.value.appInfo
                ?.contactEmail,
        )
    }
}
