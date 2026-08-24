package com.android.offread.di

import com.android.offread.BuildConfig
import com.android.offread.settings.domain.AppInfo
import com.android.offread.settings.domain.AppInfoProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * F-032: 앱 메타(BuildConfig)를 도메인 포트에 바인딩하는 컴포지션 루트 모듈.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppInfoModule {
    private const val CONTACT_EMAIL = "rugsn12345@gmail.com"

    @Provides
    @Singleton
    fun provideAppInfoProvider(): AppInfoProvider =
        object : AppInfoProvider {
            override fun get(): AppInfo =
                AppInfo(
                    versionName = BuildConfig.VERSION_NAME,
                    contactEmail = CONTACT_EMAIL,
                )
        }
}
