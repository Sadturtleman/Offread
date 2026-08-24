package com.android.offread.settings.domain

/** 정보 화면(S-06)에 노출할 앱 메타. */
data class AppInfo(
    val versionName: String,
    val contactEmail: String,
)

/** [AppInfo] 제공 포트. 실제 값(BuildConfig 등)은 컴포지션 루트(app)에서 바인딩한다. */
interface AppInfoProvider {
    fun get(): AppInfo
}
