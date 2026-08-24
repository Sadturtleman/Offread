package com.android.offread.settings.presentation.storage

import com.android.offread.core.entity.ItemType
import com.android.offread.core.entity.SerialStatus
import com.android.offread.core.entity.TranslationStatus
import com.android.offread.library.domain.model.LibraryItem
import com.android.offread.settings.domain.usecase.ClearCacheUseCase
import com.android.offread.settings.domain.usecase.ClearItemCacheUseCase
import com.android.offread.settings.domain.usecase.GetCacheStatsUseCase
import com.android.offread.settings.domain.usecase.GetDownloadedContentsUseCase
import com.android.offread.settings.presentation.FakeLibraryRepository
import com.android.offread.settings.presentation.FakeSegmentCache
import com.android.offread.settings.presentation.MainDispatcherRule
import com.android.offread.translate.domain.CacheStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StorageSettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val cache =
        FakeSegmentCache(
            mutableMapOf(
                "i1" to CacheStats(entryCount = 10, bytes = 2_000),
                "i2" to CacheStats(entryCount = 4, bytes = 9_000),
            ),
        )
    private val library = FakeLibraryRepository()

    private fun item(
        id: String,
        title: String,
    ) = LibraryItem(
        id = id,
        collectionId = "c0",
        type = ItemType.WEBNOVEL,
        title = title,
        author = "작가",
        sourceUrl = "https://ncode.syosetu.com/$id/",
        siteName = "소설가가 되자",
        totalChapters = 10,
        serialStatus = SerialStatus.ONGOING,
        translationStatus = TranslationStatus.CACHED,
        updatedAt = 0,
    )

    private fun viewModel(): StorageSettingsViewModel {
        library.seedItem(item("i1", "무직전생"))
        library.seedItem(item("i2", "전생 슬라임"))
        return StorageSettingsViewModel(
            GetCacheStatsUseCase(cache),
            GetDownloadedContentsUseCase(library, cache),
            ClearCacheUseCase(cache),
            ClearItemCacheUseCase(cache),
        )
    }

    @Test
    fun `캐시 용량과 콘텐츠 목록을 불러온다`() {
        val vm = viewModel()

        assertEquals(11_000, vm.uiState.value.cache.bytes)
        assertEquals(14, vm.uiState.value.cache.entryCount)
        assertEquals(
            listOf("i2", "i1"),
            vm.uiState.value.contents
                .map { it.item.id },
        )
    }

    @Test
    fun `전체 비우기는 확인을 거친다`() {
        val vm = viewModel()

        vm.onIntent(StorageSettingsIntent.ClearAllClicked)

        assertEquals(ClearTarget.All, vm.uiState.value.clearTarget)
        assertFalse(cache.cleared)
    }

    @Test
    fun `확인하면 캐시를 비우고 화면을 갱신한다`() {
        val vm = viewModel()
        vm.onIntent(StorageSettingsIntent.ClearAllClicked)

        vm.onIntent(StorageSettingsIntent.ConfirmClear)

        assertTrue(cache.cleared)
        assertEquals(CacheStats.EMPTY, vm.uiState.value.cache)
        assertTrue(vm.uiState.value.isEmpty)
        assertNull(vm.uiState.value.clearTarget)
    }

    @Test
    fun `작품별 비우기는 그 작품만 지운다`() {
        val vm = viewModel()
        val target =
            vm.uiState.value.contents
                .first { it.item.id == "i1" }
        vm.onIntent(StorageSettingsIntent.ClearItemClicked(target))

        vm.onIntent(StorageSettingsIntent.ConfirmClear)

        assertEquals(listOf("i1"), cache.invalidatedItems)
        assertEquals(
            listOf("i2"),
            vm.uiState.value.contents
                .map { it.item.id },
        )
        assertEquals(9_000, vm.uiState.value.cache.bytes)
    }

    @Test
    fun `취소하면 아무것도 지우지 않는다`() {
        val vm = viewModel()
        vm.onIntent(StorageSettingsIntent.ClearAllClicked)

        vm.onIntent(StorageSettingsIntent.DismissClear)

        assertNull(vm.uiState.value.clearTarget)
        assertFalse(cache.cleared)
    }
}
