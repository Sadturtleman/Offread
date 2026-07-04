package com.android.offread.library.presentation.detail

import com.android.offread.core.entity.ItemType
import com.android.offread.core.entity.SerialStatus
import com.android.offread.core.entity.TranslationStatus
import com.android.offread.library.domain.model.LibraryItem
import com.android.offread.library.domain.usecase.GetChaptersUseCase
import com.android.offread.library.domain.usecase.GetItemUseCase
import com.android.offread.library.domain.usecase.PrepareOfflineUseCase
import com.android.offread.library.presentation.FakeLibraryRepository
import com.android.offread.library.presentation.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WebNovelDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun item() =
        LibraryItem(
            id = "i0",
            collectionId = "c0",
            type = ItemType.WEBNOVEL,
            title = "무직전생",
            author = "손의 손",
            sourceUrl = "https://ncode.syosetu.com/n9669bk/",
            siteName = "소설가가 되자",
            totalChapters = 5,
            serialStatus = SerialStatus.ONGOING,
            translationStatus = TranslationStatus.UNTRANSLATED,
            updatedAt = 0,
        )

    private fun viewModel(repo: FakeLibraryRepository) =
        WebNovelDetailViewModel(GetItemUseCase(repo), GetChaptersUseCase(), PrepareOfflineUseCase(repo))

    @Test
    fun `start 시 아이템과 챕터를 노출한다`() {
        val repo = FakeLibraryRepository().apply { seedItem(item()) }
        val vm = viewModel(repo)

        vm.start("i0")

        assertEquals(
            "무직전생",
            vm.uiState.value.item
                ?.title,
        )
        assertEquals(5, vm.uiState.value.chapters.size)
    }

    @Test
    fun `오프라인 준비 시 번역 상태가 캐시됨으로 바뀐다`() {
        val repo = FakeLibraryRepository().apply { seedItem(item()) }
        val vm = viewModel(repo)
        vm.start("i0")

        vm.onIntent(WebNovelDetailIntent.PrepareOffline)

        assertEquals(
            TranslationStatus.CACHED,
            vm.uiState.value.item
                ?.translationStatus,
        )
    }

    @Test
    fun `이어읽기는 리더 열기 이펙트를 낸다`() =
        runTest {
            val repo = FakeLibraryRepository().apply { seedItem(item()) }
            val vm = viewModel(repo)
            vm.start("i0")

            vm.onIntent(WebNovelDetailIntent.ContinueReading)

            val effect = vm.effect.first()
            assertTrue(effect is WebNovelDetailEffect.OpenReader)
            effect as WebNovelDetailEffect.OpenReader
            assertEquals("i0", effect.itemId)
            // lastReadChapter 0 → 1화로 시작.
            assertEquals(1, effect.chapterIndex)
        }
}
