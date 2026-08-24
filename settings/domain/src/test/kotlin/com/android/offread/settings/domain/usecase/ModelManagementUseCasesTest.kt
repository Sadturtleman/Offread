package com.android.offread.settings.domain.usecase

import com.android.offread.core.entity.LanguagePair
import com.android.offread.settings.domain.FakeTranslationModelRepository
import com.android.offread.translate.domain.model.ModelDownloadStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelManagementUseCasesTest {
    private val repo = FakeTranslationModelRepository(installed = setOf(LanguagePair.JA_KO))

    @Test
    fun `선택 가능한 언어쌍만 목록에 나오고 설치 여부가 표시된다`() =
        runTest {
            val models = ObserveManagedModelsUseCase(repo)().first()

            assertEquals(listOf(LanguagePair.JA_KO, LanguagePair.ZH_KO), models.map { it.model.languagePair })
            assertTrue(models.first { it.model.languagePair == LanguagePair.JA_KO }.installed)
            assertFalse(models.first { it.model.languagePair == LanguagePair.ZH_KO }.installed)
        }

    @Test
    fun `제공 예정 언어쌍은 목록에 없다`() =
        runTest {
            val models = ObserveManagedModelsUseCase(repo)().first()

            assertTrue(models.none { it.model.languagePair == LanguagePair.EN_KO })
        }

    @Test
    fun `진행 중 상태가 목록에 반영된다`() =
        runTest {
            repo.emitDownloads(
                mapOf("model-zh_ko" to ModelDownloadStatus.Downloading(50, 100, 10)),
            )

            val zhKo = ObserveManagedModelsUseCase(repo)().first().first { it.model.id == "model-zh_ko" }

            assertTrue(zhKo.isBusy)
            assertEquals(0.5f, (zhKo.status as ModelDownloadStatus.Downloading).fraction, 0.001f)
        }

    @Test
    fun `다운로드는 해당 모델만 큐에 넣는다`() =
        runTest {
            val zhKo = FakeTranslationModelRepository.model(LanguagePair.ZH_KO)

            DownloadModelUseCase(repo)(zhKo)

            assertEquals(listOf(zhKo), repo.enqueued)
        }

    @Test
    fun `삭제하면 설치 목록에서 빠진다`() =
        runTest {
            DeleteModelUseCase(repo)("model-ja_ko")

            assertEquals(listOf("model-ja_ko"), repo.deleted)
            assertFalse(ObserveManagedModelsUseCase(repo)().first().first { it.model.id == "model-ja_ko" }.installed)
        }
}
