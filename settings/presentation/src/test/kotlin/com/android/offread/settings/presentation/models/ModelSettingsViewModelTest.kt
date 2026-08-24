package com.android.offread.settings.presentation.models

import com.android.offread.core.entity.LanguagePair
import com.android.offread.core.entity.TranslationModel
import com.android.offread.settings.domain.model.ManagedModel
import com.android.offread.settings.domain.usecase.DeleteModelUseCase
import com.android.offread.settings.domain.usecase.DownloadModelUseCase
import com.android.offread.settings.domain.usecase.ObserveManagedModelsUseCase
import com.android.offread.settings.presentation.MainDispatcherRule
import com.android.offread.translate.domain.TranslationModelRepository
import com.android.offread.translate.domain.model.ModelDownloadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private class FakeModelRepository(
    installed: Set<LanguagePair> = emptySet(),
) : TranslationModelRepository {
    private val installedState = MutableStateFlow(installed)
    private val downloads = MutableStateFlow<Map<String, ModelDownloadStatus>>(emptyMap())

    val enqueued = mutableListOf<TranslationModel>()
    val deleted = mutableListOf<String>()

    override fun catalogFor(pairs: Set<LanguagePair>): List<TranslationModel> =
        pairs.filter { it.isSelectable }.sortedBy { it.name }.map { pair ->
            TranslationModel(
                id = "model-${pair.name.lowercase()}",
                languagePair = pair,
                displayName = "TranslateGemma 4B",
                sizeBytes = pair.modelSizeBytes,
                version = "1.0",
                sha256 = "test",
            )
        }

    override val installedLanguagePairs: Flow<Set<LanguagePair>> = installedState.asStateFlow()

    override fun observeDownloads(): Flow<Map<String, ModelDownloadStatus>> = downloads.asStateFlow()

    override suspend fun enqueue(models: List<TranslationModel>) {
        enqueued += models
    }

    override suspend fun pause(modelId: String) = Unit

    override suspend fun resume(modelId: String) = Unit

    override suspend fun delete(modelId: String) {
        deleted += modelId
        installedState.value = installedState.value.filterNot { "model-${it.name.lowercase()}" == modelId }.toSet()
    }
}

class ModelSettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(repo: FakeModelRepository) =
        ModelSettingsViewModel(
            ObserveManagedModelsUseCase(repo),
            DownloadModelUseCase(repo),
            DeleteModelUseCase(repo),
        )

    @Test
    fun `설치된 모델의 용량을 합산해 보여준다`() {
        val repo = FakeModelRepository(installed = setOf(LanguagePair.JA_KO))

        val vm = viewModel(repo)

        assertEquals(LanguagePair.JA_KO.modelSizeBytes, vm.uiState.value.installedBytes)
        assertEquals(2, vm.uiState.value.models.size)
    }

    @Test
    fun `내려받기는 해당 모델만 큐에 넣는다`() {
        val repo = FakeModelRepository()
        val vm = viewModel(repo)
        val zhKo =
            vm.uiState.value.models
                .first { it.model.languagePair == LanguagePair.ZH_KO }

        vm.onIntent(ModelSettingsIntent.Download(zhKo))

        assertEquals(listOf(zhKo.model), repo.enqueued)
    }

    @Test
    fun `삭제는 확인을 거친다`() {
        val repo = FakeModelRepository(installed = setOf(LanguagePair.JA_KO))
        val vm = viewModel(repo)
        val jaKo =
            vm.uiState.value.models
                .first { it.model.languagePair == LanguagePair.JA_KO }

        vm.onIntent(ModelSettingsIntent.DeleteClicked(jaKo))

        assertEquals(
            jaKo.model.id,
            vm.uiState.value.deleteTarget
                ?.model
                ?.id,
        )
        assertTrue(repo.deleted.isEmpty())
    }

    @Test
    fun `확인하면 모델을 지우고 목록에서 설치 표시가 사라진다`() {
        val repo = FakeModelRepository(installed = setOf(LanguagePair.JA_KO))
        val vm = viewModel(repo)
        val jaKo =
            vm.uiState.value.models
                .first { it.model.languagePair == LanguagePair.JA_KO }
        vm.onIntent(ModelSettingsIntent.DeleteClicked(jaKo))

        vm.onIntent(ModelSettingsIntent.ConfirmDelete)

        assertEquals(listOf("model-ja_ko"), repo.deleted)
        assertNull(vm.uiState.value.deleteTarget)
        assertFalse(
            vm.uiState.value.models
                .first { it.model.id == "model-ja_ko" }
                .installed,
        )
        assertEquals(0L, vm.uiState.value.installedBytes)
    }

    @Test
    fun `취소하면 지우지 않는다`() {
        val repo = FakeModelRepository(installed = setOf(LanguagePair.JA_KO))
        val vm = viewModel(repo)
        val jaKo =
            vm.uiState.value.models
                .first { it.model.languagePair == LanguagePair.JA_KO }
        vm.onIntent(ModelSettingsIntent.DeleteClicked(jaKo))

        vm.onIntent(ModelSettingsIntent.DismissDelete)

        assertNull(vm.uiState.value.deleteTarget)
        assertTrue(repo.deleted.isEmpty())
    }

    @Test
    fun `ManagedModel 은 진행 중 여부를 상태로 판단한다`() {
        val model = FakeModelRepository().catalogFor(setOf(LanguagePair.JA_KO)).single()

        assertTrue(ManagedModel(model, installed = false, status = ModelDownloadStatus.Queued).isBusy)
        assertFalse(ManagedModel(model, installed = false, status = null).isBusy)
        assertFalse(ManagedModel(model, installed = true, status = ModelDownloadStatus.Completed).isBusy)
    }
}
