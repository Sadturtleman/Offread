package com.android.offread.library.domain.usecase

import com.android.offread.core.entity.ItemType
import com.android.offread.core.entity.SerialStatus
import com.android.offread.core.entity.TranslationStatus
import com.android.offread.library.domain.FakeLibraryRepository
import com.android.offread.library.domain.model.LibraryItem
import com.android.offread.translate.domain.PretranslateRequest
import com.android.offread.translate.domain.PretranslateScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakePretranslateScheduler : PretranslateScheduler {
    val scheduled = mutableListOf<PretranslateRequest>()
    val cancelled = mutableListOf<String>()

    override fun schedule(request: PretranslateRequest) {
        scheduled += request
    }

    override fun cancel(itemId: String) {
        cancelled += itemId
    }
}

class PrepareOfflineUseCaseTest {
    private val repo = FakeLibraryRepository()
    private val scheduler = FakePretranslateScheduler()
    private val prepareOffline = PrepareOfflineUseCase(repo, scheduler)

    private fun seed(
        totalChapters: Int,
        lastReadChapter: Int,
    ) {
        repo.seedItem(
            LibraryItem(
                id = "i0",
                collectionId = "c0",
                type = ItemType.WEBNOVEL,
                title = "무직전생",
                author = "손의 손",
                sourceUrl = "https://ncode.syosetu.com/n9669bk/",
                siteName = "소설가가 되자",
                totalChapters = totalChapters,
                serialStatus = SerialStatus.ONGOING,
                translationStatus = TranslationStatus.UNTRANSLATED,
                updatedAt = 0,
                lastReadChapter = lastReadChapter,
            ),
        )
    }

    @Test
    fun `읽던 다음 화부터 기본 5화를 예약한다`() =
        runTest {
            seed(totalChapters = 100, lastReadChapter = 10)

            val scheduledCount = prepareOffline("i0")

            assertEquals(5, scheduledCount)
            val request = scheduler.scheduled.single()
            assertEquals(11, request.fromChapter)
            assertEquals(5, request.count)
            assertEquals("c0", request.collectionId)
        }

    @Test
    fun `아직 읽지 않았으면 1화부터 예약한다`() =
        runTest {
            seed(totalChapters = 100, lastReadChapter = 0)

            prepareOffline("i0")

            assertEquals(1, scheduler.scheduled.single().fromChapter)
        }

    @Test
    fun `남은 화가 적으면 남은 만큼만 예약한다`() =
        runTest {
            seed(totalChapters = 12, lastReadChapter = 10)

            val scheduledCount = prepareOffline("i0")

            assertEquals(2, scheduledCount)
            assertEquals(2, scheduler.scheduled.single().count)
        }

    @Test
    fun `마지막 화까지 읽었으면 예약하지 않는다`() =
        runTest {
            seed(totalChapters = 10, lastReadChapter = 10)

            val scheduledCount = prepareOffline("i0")

            assertEquals(0, scheduledCount)
            assertTrue(scheduler.scheduled.isEmpty())
            assertEquals(TranslationStatus.UNTRANSLATED, repo.observeItem("i0").first()?.translationStatus)
        }

    @Test
    fun `예약하면 배지를 번역 중으로 바꾼다`() =
        runTest {
            seed(totalChapters = 100, lastReadChapter = 0)

            prepareOffline("i0")

            assertEquals(TranslationStatus.TRANSLATING, repo.observeItem("i0").first()?.translationStatus)
        }

    @Test
    fun `없는 아이템은 예약하지 않는다`() =
        runTest {
            assertEquals(0, prepareOffline("missing"))
            assertTrue(scheduler.scheduled.isEmpty())
        }
}
