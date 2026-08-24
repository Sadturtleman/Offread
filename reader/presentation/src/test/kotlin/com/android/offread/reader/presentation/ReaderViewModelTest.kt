package com.android.offread.reader.presentation

import com.android.offread.library.domain.usecase.GetItemUseCase
import com.android.offread.reader.domain.model.ReaderTheme
import com.android.offread.reader.domain.usecase.GetChapterContentUseCase
import com.android.offread.reader.domain.usecase.RetrySegmentUseCase
import com.android.offread.reader.domain.usecase.SaveReadingProgressUseCase
import com.android.offread.terms.domain.usecase.UpsertTermUseCase
import com.android.offread.translate.domain.TermCandidateExtractor
import com.android.offread.translate.domain.usecase.InvalidateChapterCacheUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ReaderViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val termRepository = FakeTermRepository()
    private val segmentCache = FakeSegmentCache()
    private val chapterContent = FakeChapterContentRepository()

    private fun viewModel(repo: FakeLibraryRepository): ReaderViewModel =
        ReaderViewModel(
            GetItemUseCase(repo),
            GetChapterContentUseCase(chapterContent),
            RetrySegmentUseCase(FakeChapterContentRepository()),
            SaveReadingProgressUseCase(repo),
            UpsertTermUseCase(termRepository),
            InvalidateChapterCacheUseCase(segmentCache),
            TermCandidateExtractor(),
        )

    @Test
    fun `start 시 헤더와 본문을 노출하고 읽던 위치를 저장한다`() {
        val repo = FakeLibraryRepository(testItem(totalChapters = 286))
        val vm = viewModel(repo)

        vm.start("i0", 124)

        assertEquals("무직전생", vm.uiState.value.itemTitle)
        assertEquals(124, vm.uiState.value.chapterIndex)
        assertEquals(124, repo.current("i0")?.lastReadChapter)
        assertTrue(vm.uiState.value.hasPrevious)
        assertTrue(vm.uiState.value.hasNext)
    }

    @Test
    fun `다음 화로 이동하면 화 번호가 증가하고 위치가 저장된다`() {
        val repo = FakeLibraryRepository(testItem(totalChapters = 286))
        val vm = viewModel(repo)
        vm.start("i0", 124)

        vm.onIntent(ReaderIntent.NextChapter)

        assertEquals(125, vm.uiState.value.chapterIndex)
        assertEquals(125, repo.current("i0")?.lastReadChapter)
    }

    @Test
    fun `마지막 화에서는 다음 화가 없다`() {
        val repo = FakeLibraryRepository(testItem(totalChapters = 5))
        val vm = viewModel(repo)

        vm.start("i0", 5)

        assertFalse(vm.uiState.value.hasNext)
    }

    @Test
    fun `미번역 세그먼트를 재시도하면 번역문이 채워진다`() {
        val repo = FakeLibraryRepository(testItem())
        val vm = viewModel(repo)
        vm.start("i0", 1)
        assertNull(
            vm.uiState.value.content
                ?.segments
                ?.first { it.id == "s2" }
                ?.translated,
        )

        vm.onIntent(ReaderIntent.RetrySegment("s2"))

        assertEquals(
            "재번역-s2",
            vm.uiState.value.content
                ?.segments
                ?.first { it.id == "s2" }
                ?.translated,
        )
    }

    @Test
    fun `설정에서 폰트 배율과 테마를 바꾼다`() {
        val repo = FakeLibraryRepository(testItem())
        val vm = viewModel(repo)
        vm.start("i0", 1)

        vm.onIntent(ReaderIntent.ChangeFontScale(1.4f))
        vm.onIntent(ReaderIntent.ChangeTheme(ReaderTheme.SEPIA))

        assertEquals(1.4f, vm.uiState.value.settings.fontScale, 0.001f)
        assertEquals(ReaderTheme.SEPIA, vm.uiState.value.settings.theme)
    }

    @Test
    fun `폰트 배율은 허용 범위로 제한된다`() {
        val repo = FakeLibraryRepository(testItem())
        val vm = viewModel(repo)
        vm.start("i0", 1)

        vm.onIntent(ReaderIntent.ChangeFontScale(5.0f))

        assertEquals(1.6f, vm.uiState.value.settings.fontScale, 0.001f)
    }

    @Test
    fun `본문 롱프레스는 등록할 표기 후보를 띄운다`() {
        val vm = viewModel(FakeLibraryRepository(testItem(totalChapters = 5)))
        vm.start("i0", 1)

        vm.onIntent(ReaderIntent.LongPressSegment("s1"))

        assertEquals(listOf("原文"), (vm.uiState.value.quickEdit as TermQuickEdit.PickWord).words)
    }

    @Test
    fun `표기를 고르면 용어 편집으로 넘어간다`() {
        val vm = viewModel(FakeLibraryRepository(testItem(totalChapters = 5)))
        vm.start("i0", 1)
        vm.onIntent(ReaderIntent.LongPressSegment("s1"))

        vm.onIntent(ReaderIntent.PickWord("原文"))

        assertEquals("原文", (vm.uiState.value.quickEdit as TermQuickEdit.Edit).source)
    }

    @Test
    fun `저장하면 컬렉션 용어맵에 남고 재번역을 확인한다`() {
        val vm = viewModel(FakeLibraryRepository(testItem(totalChapters = 5)))
        vm.start("i0", 1)
        vm.onIntent(ReaderIntent.LongPressSegment("s1"))
        vm.onIntent(ReaderIntent.PickWord("原文"))

        vm.onIntent(ReaderIntent.SubmitTerm(translation = "원문", pinned = true))

        val saved = termRepository.current().single()
        assertEquals("原文", saved.source)
        assertEquals("원문", saved.translation)
        assertTrue(saved.pinned)
        assertEquals("c0", saved.collectionId)
        assertEquals(TermQuickEdit.ConfirmRetranslate, vm.uiState.value.quickEdit)
    }

    @Test
    fun `다시 번역을 고르면 그 화의 캐시를 버리고 본문을 다시 불러온다`() {
        val vm = viewModel(FakeLibraryRepository(testItem(totalChapters = 5)))
        vm.start("i0", 3)
        vm.onIntent(ReaderIntent.LongPressSegment("s1"))
        vm.onIntent(ReaderIntent.PickWord("原文"))
        vm.onIntent(ReaderIntent.SubmitTerm("원문", pinned = false))
        val loadsBefore = chapterContent.getChapterCalls

        vm.onIntent(ReaderIntent.ConfirmRetranslate(retranslate = true))

        assertEquals(listOf("i0" to 3), segmentCache.invalidatedChapters)
        assertEquals(loadsBefore + 1, chapterContent.getChapterCalls)
        assertNull(vm.uiState.value.quickEdit)
    }

    @Test
    fun `나중에를 고르면 캐시를 건드리지 않는다`() {
        val vm = viewModel(FakeLibraryRepository(testItem(totalChapters = 5)))
        vm.start("i0", 1)
        vm.onIntent(ReaderIntent.LongPressSegment("s1"))
        vm.onIntent(ReaderIntent.PickWord("原文"))
        vm.onIntent(ReaderIntent.SubmitTerm("원문", pinned = false))

        vm.onIntent(ReaderIntent.ConfirmRetranslate(retranslate = false))

        assertTrue(segmentCache.invalidatedChapters.isEmpty())
        assertNull(vm.uiState.value.quickEdit)
    }
}
