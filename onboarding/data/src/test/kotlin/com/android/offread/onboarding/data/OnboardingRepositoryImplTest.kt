package com.android.offread.onboarding.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.android.offread.core.entity.LanguagePair
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class OnboardingRepositoryImplTest {
    private lateinit var tempFile: File
    private lateinit var scope: TestScope
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: OnboardingRepositoryImpl

    @Before
    fun setUp() {
        tempFile = File.createTempFile("onboarding_test", ".preferences_pb").apply { delete() }
        scope = TestScope(StandardTestDispatcher())
        dataStore =
            PreferenceDataStoreFactory.create(
                scope = scope,
                produceFile = { tempFile },
            )
        repository = OnboardingRepositoryImpl(dataStore)
    }

    @After
    fun tearDown() {
        tempFile.delete()
    }

    @Test
    fun `기본값은 온보딩 미완료·언어쌍 없음`() =
        runTest(scope.testScheduler) {
            assertFalse(repository.isOnboardingComplete.first())
            assertTrue(repository.selectedLanguagePairs.first().isEmpty())
        }

    @Test
    fun `언어쌍을 저장하고 다시 읽는다`() =
        runTest(scope.testScheduler) {
            repository.setSelectedLanguagePairs(setOf(LanguagePair.JA_KO, LanguagePair.ZH_KO))

            assertEquals(
                setOf(LanguagePair.JA_KO, LanguagePair.ZH_KO),
                repository.selectedLanguagePairs.first(),
            )
        }

    @Test
    fun `온보딩 완료 플래그를 저장하고 다시 읽는다`() =
        runTest(scope.testScheduler) {
            repository.setOnboardingComplete(true)

            assertTrue(repository.isOnboardingComplete.first())
        }
}
