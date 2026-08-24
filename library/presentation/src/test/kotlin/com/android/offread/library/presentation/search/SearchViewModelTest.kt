package com.android.offread.library.presentation.search

import com.android.offread.core.entity.ItemType
import com.android.offread.core.entity.SerialStatus
import com.android.offread.core.entity.TranslationStatus
import com.android.offread.library.domain.model.LibraryItem
import com.android.offread.library.domain.usecase.ObserveCollectionsUseCase
import com.android.offread.library.domain.usecase.SearchItemsUseCase
import com.android.offread.library.presentation.FakeLibraryRepository
import com.android.offread.library.presentation.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    private val repo = FakeLibraryRepository()

    private fun viewModel() = SearchViewModel(SearchItemsUseCase(repo), ObserveCollectionsUseCase(repo))

    private fun item(
        id: String,
        title: String,
        author: String,
        collectionId: String = "c0",
        type: ItemType = ItemType.WEBNOVEL,
    ) = LibraryItem(
        id = id,
        collectionId = collectionId,
        type = type,
        title = title,
        author = author,
        sourceUrl = "https://ncode.syosetu.com/$id/",
        siteName = "소설가가 되자",
        totalChapters = 10,
        serialStatus = SerialStatus.ONGOING,
        translationStatus = TranslationStatus.UNTRANSLATED,
        updatedAt = 0,
    )

    private fun seed() {
        repo.seedItem(item("i1", "무직전생", "손의 손"))
        repo.seedItem(item("i2", "전생했더니 슬라임", "후세", collectionId = "c1"))
        repo.seedItem(item("i3", "Attention Is All You Need", "Vaswani", type = ItemType.PAPER))
    }

    @Test
    fun `입력 전에는 검색하지 않은 상태다`() =
        runTest(dispatcher.scheduler) {
            seed()
            val vm = viewModel()

            vm.start(null)
            advanceTimeBy(DEBOUNCE_PADDING)
            runCurrent()

            assertFalse(vm.uiState.value.searched)
            assertTrue(
                vm.uiState.value.results
                    .isEmpty(),
            )
        }

    @Test
    fun `디바운스 뒤 제목 검색 결과가 상태에 실린다`() =
        runTest(dispatcher.scheduler) {
            seed()
            val vm = viewModel()
            vm.start(null)

            vm.onIntent(SearchIntent.ChangeText("전생"))
            assertEquals("전생", vm.uiState.value.text)
            advanceTimeBy(DEBOUNCE_PADDING)
            runCurrent()

            assertTrue(vm.uiState.value.searched)
            assertEquals(
                listOf("i1", "i2"),
                vm.uiState.value.results
                    .map { it.id }
                    .sorted(),
            )
        }

    @Test
    fun `컬렉션 스코프로 시작하면 해당 컬렉션만 검색한다`() =
        runTest(dispatcher.scheduler) {
            seed()
            val vm = viewModel()
            vm.start("c1")

            vm.onIntent(SearchIntent.ChangeText("전생"))
            advanceTimeBy(DEBOUNCE_PADDING)
            runCurrent()

            assertEquals("c1", vm.uiState.value.collectionId)
            assertEquals(
                listOf("i2"),
                vm.uiState.value.results
                    .map { it.id },
            )
        }

    @Test
    fun `유형 필터를 바꾸면 다시 검색한다`() =
        runTest(dispatcher.scheduler) {
            seed()
            val vm = viewModel()
            vm.start(null)
            vm.onIntent(SearchIntent.ChangeText("a"))
            advanceTimeBy(DEBOUNCE_PADDING)
            runCurrent()

            vm.onIntent(SearchIntent.ChangeType(ItemType.PAPER))
            advanceTimeBy(DEBOUNCE_PADDING)
            runCurrent()

            assertEquals(ItemType.PAPER, vm.uiState.value.type)
            assertEquals(
                listOf("i3"),
                vm.uiState.value.results
                    .map { it.id },
            )
        }

    @Test
    fun `지우기는 결과와 검색 상태를 되돌린다`() =
        runTest(dispatcher.scheduler) {
            seed()
            val vm = viewModel()
            vm.start(null)
            vm.onIntent(SearchIntent.ChangeText("전생"))
            advanceTimeBy(DEBOUNCE_PADDING)
            runCurrent()

            vm.onIntent(SearchIntent.ClearText)
            advanceTimeBy(DEBOUNCE_PADDING)
            runCurrent()

            assertEquals("", vm.uiState.value.text)
            assertFalse(vm.uiState.value.searched)
            assertTrue(
                vm.uiState.value.results
                    .isEmpty(),
            )
        }

    private companion object {
        const val DEBOUNCE_PADDING = 400L
    }
}
