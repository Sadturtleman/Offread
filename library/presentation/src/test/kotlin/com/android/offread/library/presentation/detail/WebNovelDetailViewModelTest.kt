package com.android.offread.library.presentation.detail

import com.android.offread.core.entity.ItemType
import com.android.offread.core.entity.SerialStatus
import com.android.offread.core.entity.TranslationStatus
import com.android.offread.library.domain.ChapterSource
import com.android.offread.library.domain.TranslationCache
import com.android.offread.library.domain.model.LibraryItem
import com.android.offread.library.domain.model.TermMapMoveStrategy
import com.android.offread.library.domain.usecase.GetChaptersUseCase
import com.android.offread.library.domain.usecase.GetItemUseCase
import com.android.offread.library.domain.usecase.MoveItemUseCase
import com.android.offread.library.domain.usecase.ObserveCollectionsUseCase
import com.android.offread.library.domain.usecase.PrepareOfflineUseCase
import com.android.offread.library.domain.usecase.RefreshChaptersUseCase
import com.android.offread.library.presentation.FakeLibraryRepository
import com.android.offread.library.presentation.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private class FakeChapterSource(
    var totalChapters: Int = 5,
    var error: Throwable? = null,
) : ChapterSource {
    override suspend fun fetchTotalChapters(sourceUrl: String): Int = error?.let { throw it } ?: totalChapters
}

private class FakeTranslationCache : TranslationCache {
    val invalidatedItemIds = mutableListOf<String>()

    override suspend fun invalidateItem(itemId: String) {
        invalidatedItemIds += itemId
    }
}

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

    private fun viewModel(
        repo: FakeLibraryRepository,
        cache: FakeTranslationCache = FakeTranslationCache(),
        source: FakeChapterSource = FakeChapterSource(),
    ) = WebNovelDetailViewModel(
        GetItemUseCase(repo),
        GetChaptersUseCase(),
        PrepareOfflineUseCase(repo),
        ObserveCollectionsUseCase(repo),
        MoveItemUseCase(repo, cache),
        RefreshChaptersUseCase(repo, source),
    )

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
    fun `이동 다이얼로그를 열고 닫는다`() {
        val repo = FakeLibraryRepository().apply { seedItem(item()) }
        val vm = viewModel(repo)
        vm.start("i0")

        vm.onIntent(WebNovelDetailIntent.MoveClicked)
        assertTrue(vm.uiState.value.moveDialogVisible)

        vm.onIntent(WebNovelDetailIntent.DismissMoveDialog)
        assertFalse(vm.uiState.value.moveDialogVisible)
    }

    @Test
    fun `이동하면 아이템 컬렉션이 바뀌고 다이얼로그가 닫히고 캐시가 무효화된다`() =
        runTest {
            val repo = FakeLibraryRepository().apply { seedItem(item().copy(collectionId = "shelf")) }
            val target = repo.createCollection("이세계물", null)
            val cache = FakeTranslationCache()
            val vm = viewModel(repo, cache)
            vm.start("i0")
            vm.onIntent(WebNovelDetailIntent.MoveClicked)

            vm.onIntent(WebNovelDetailIntent.SubmitMove(target, TermMapMoveStrategy.MOVE))

            assertEquals(
                target,
                vm.uiState.value.item
                    ?.collectionId,
            )
            assertFalse(vm.uiState.value.moveDialogVisible)
            assertEquals(TermMapMoveStrategy.MOVE, repo.lastMoveStrategy)
            assertEquals(listOf("i0"), cache.invalidatedItemIds)
        }

    @Test
    fun `같은 컬렉션으로 이동하면 실패 메시지를 낸다`() =
        runTest {
            val repo = FakeLibraryRepository().apply { seedItem(item()) }
            val vm = viewModel(repo)
            vm.start("i0")
            vm.onIntent(WebNovelDetailIntent.MoveClicked)

            vm.onIntent(WebNovelDetailIntent.SubmitMove("c0", TermMapMoveStrategy.LEAVE))

            val effect = vm.effect.first()
            assertTrue(effect is WebNovelDetailEffect.ShowMessage)
            assertTrue(vm.uiState.value.moveDialogVisible)
            assertEquals(
                "c0",
                vm.uiState.value.item
                    ?.collectionId,
            )
        }

    @Test
    fun `start 시 컬렉션 목록을 구독한다`() =
        runTest {
            val repo = FakeLibraryRepository().apply { seedItem(item()) }
            repo.createCollection("판타지", null)
            val vm = viewModel(repo)

            vm.start("i0")

            assertEquals(1, vm.uiState.value.collections.size)
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

    @Test
    fun `새 화 확인은 화수를 갱신하고 챕터 목록에 반영한다`() =
        runTest {
            val repo = FakeLibraryRepository()
            repo.seedItem(item())
            val vm = viewModel(repo, source = FakeChapterSource(totalChapters = 8))
            vm.start("i0")

            vm.onIntent(WebNovelDetailIntent.CheckForNewChapters)

            assertEquals("새 3화를 찾았어요.", (vm.effect.first() as WebNovelDetailEffect.ShowMessage).message)
            assertEquals(
                8,
                vm.uiState.value.item
                    ?.totalChapters,
            )
            assertEquals(8, vm.uiState.value.chapters.size)
            assertFalse(vm.uiState.value.refreshing)
        }

    @Test
    fun `새 화가 없으면 그대로 알린다`() =
        runTest {
            val repo = FakeLibraryRepository()
            repo.seedItem(item())
            val vm = viewModel(repo, source = FakeChapterSource(totalChapters = 5))
            vm.start("i0")

            vm.onIntent(WebNovelDetailIntent.CheckForNewChapters)

            assertEquals("새로 올라온 화가 없어요.", (vm.effect.first() as WebNovelDetailEffect.ShowMessage).message)
            assertEquals(
                5,
                vm.uiState.value.item
                    ?.totalChapters,
            )
        }

    @Test
    fun `수집 실패는 메시지로 알린다`() =
        runTest {
            val repo = FakeLibraryRepository()
            repo.seedItem(item())
            val source = FakeChapterSource(error = IllegalArgumentException("지원하지 않는 사이트예요."))
            val vm = viewModel(repo, source = source)
            vm.start("i0")

            vm.onIntent(WebNovelDetailIntent.CheckForNewChapters)

            assertEquals("지원하지 않는 사이트예요.", (vm.effect.first() as WebNovelDetailEffect.ShowMessage).message)
            assertFalse(vm.uiState.value.refreshing)
        }
}
