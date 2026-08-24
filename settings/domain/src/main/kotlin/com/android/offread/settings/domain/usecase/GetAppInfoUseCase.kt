package com.android.offread.settings.domain.usecase

import com.android.offread.settings.domain.AppInfo
import com.android.offread.settings.domain.AppInfoProvider
import javax.inject.Inject

/** F-032: 정보 화면에 노출할 앱 메타. */
class GetAppInfoUseCase
    @Inject
    constructor(
        private val appInfoProvider: AppInfoProvider,
    ) {
        operator fun invoke(): AppInfo = appInfoProvider.get()
    }
