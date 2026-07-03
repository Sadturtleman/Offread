package com.android.offread.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 앱 전역 Preferences DataStore 제공(P-01 온디바이스 로컬 저장).
 * feature data 모듈은 이 단일 DataStore 를 주입받아 각자의 키 스페이스로 자기 포트를 구현한다.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    private const val STORE_NAME = "offread_prefs"

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile(STORE_NAME) },
        )
}
