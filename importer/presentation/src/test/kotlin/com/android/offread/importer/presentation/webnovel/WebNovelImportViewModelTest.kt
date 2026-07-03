package com.android.offread.importer.presentation.webnovel

import com.android.offread.importer.domain.usecase.RecognizeWebNovelUseCase
import com.android.offread.importer.presentation.FakeLibraryRepository
import com.android.offread.importer.presentation.FakeWebNovelImporter
import com.android.offread.importer.presentation.MainDispatcherRule
import com.android.offread.library.domain.usecase.AddWebNovelUseCase
import com.android.offread.library.domain.usecase.CreateCollectionUseCase
import com.android.offread.library.domain.usecase.ObserveCollectionsUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WebNovelImportViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        repo: FakeLibraryRepository,
        supported: Boolean = true,
    ) = WebNovelImportViewModel(
        RecognizeWebNovelUseCase(FakeWebNovelImporter(supported = supported)),
        ObserveCollectionsUseCase(repo),
        CreateCollectionUseCase(repo),
        AddWebNovelUseCase(repo),
    )

    @Test
    fun `첫 컬렉션이 기본 선택된다`() {
        val repo = FakeLibraryRepository()
        val id = repo.seedCollection("이세계물")
        val vm = viewModel(repo)

        assertEquals(id, vm.uiState.value.selectedCollectionId)
    }

    @Test
    fun `URL 인식 후 메타를 노출하고 가져오기가 가능해진다`() {
        val repo = FakeLibraryRepository()
        repo.seedCollection("이세계물")
        val vm = viewModel(repo)

        vm.onIntent(WebNovelImportIntent.UrlChanged("https://ncode.syosetu.com/n9669bk/"))
        vm.onIntent(WebNovelImportIntent.Recognize)

        assertEquals(
            "무직전생",
            vm.uiState.value.metadata
                ?.title,
        )
        assertTrue(vm.uiState.value.canImport)
    }

    @Test
    fun `가져오기가 아이템을 저장하고 완료 이펙트를 낸다`() =
        runTest {
            val repo = FakeLibraryRepository()
            repo.seedCollection("이세계물")
            val vm = viewModel(repo)
            vm.onIntent(WebNovelImportIntent.UrlChanged("https://ncode.syosetu.com/n9669bk/"))
            vm.onIntent(WebNovelImportIntent.Recognize)

            vm.onIntent(WebNovelImportIntent.Import)

            assertEquals(WebNovelImportEffect.Done, vm.effect.first())
            assertEquals("무직전생", repo.currentItems().single().title)
        }

    @Test
    fun `미지원 사이트는 에러 이펙트를 낸다`() =
        runTest {
            val repo = FakeLibraryRepository()
            val vm = viewModel(repo, supported = false)
            vm.onIntent(WebNovelImportIntent.UrlChanged("https://example.com/x"))

            vm.onIntent(WebNovelImportIntent.Recognize)

            assertTrue(vm.effect.first() is WebNovelImportEffect.ShowError)
        }

    @Test
    fun `새 컬렉션을 만들면 선택된다`() {
        val repo = FakeLibraryRepository()
        val vm = viewModel(repo)

        vm.onIntent(WebNovelImportIntent.CreateCollection("새 폴더"))

        assertTrue(vm.uiState.value.selectedCollectionId != null)
        assertEquals(
            "새 폴더",
            vm.uiState.value.collections
                .single()
                .name,
        )
    }
}
