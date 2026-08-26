package com.android.offread.translate.domain.usecase

import com.android.offread.core.entity.LanguagePair
import com.android.offread.translate.domain.FakeSegmentCache
import com.android.offread.translate.domain.FakeTranslationEngine
import com.android.offread.translate.domain.FakeWebPageSource
import com.android.offread.translate.domain.SegmentSplitter
import com.android.offread.translate.domain.TranslationEngine
import com.android.offread.translate.domain.model.Segment
import com.android.offread.translate.domain.model.SegmentCacheKey
import com.android.offread.translate.domain.model.WebPage
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslatePageUseCaseTest {
    private val cache = FakeSegmentCache()
    private val url = "https://example.com/novel/1"

    private fun useCase(
        engine: TranslationEngine = FakeTranslationEngine(),
        source: FakeWebPageSource = FakeWebPageSource(),
    ) = TranslatePageUseCase(source, SegmentSplitter(), engine, cache)

    @Test
    fun `페이지를 받아 문단마다 번역한다`() =
        runTest {
            val page = useCase()(url, LanguagePair.JA_KO)

            assertEquals(listOf("번역:첫 문단.", "번역:둘째 문단."), page.segments.map { it.translated })
            assertEquals("제목", page.title)
            assertEquals(url, page.url)
            assertEquals(2, cache.puts.size)
        }

    @Test
    fun `캐시가 히트하면 추론을 건너뛴다`() =
        runTest {
            cache.seed(SegmentCacheKey.of("첫 문단.", "v1"), "캐시된 번역")
            val engine = FakeTranslationEngine()

            val page = useCase(engine)(url, LanguagePair.JA_KO)

            assertEquals("캐시된 번역", page.segments.first().translated)
            assertTrue(page.segments.first().fromCache)
            assertEquals(listOf("둘째 문단."), engine.translatedTexts)
        }

    @Test
    fun `모델이 바뀌면 옛 캐시를 쓰지 않는다`() =
        runTest {
            cache.seed(SegmentCacheKey.of("첫 문단.", "v1"), "옛 번역")

            val page = useCase(FakeTranslationEngine(version = "v2"))(url, LanguagePair.JA_KO)

            assertEquals("번역:첫 문단.", page.segments.first().translated)
            assertFalse(page.segments.first().fromCache)
        }

    @Test
    fun `문단 하나가 실패해도 나머지는 계속 번역한다`() =
        runTest {
            val engine =
                object : TranslationEngine {
                    override suspend fun translate(
                        text: String,
                        pair: LanguagePair,
                    ): String {
                        if (text == "둘째 문단.") throw IllegalStateException("모델 미설치")
                        return "번역:$text"
                    }

                    override suspend fun modelVersion(pair: LanguagePair): String = "v1"
                }

            val page = useCase(engine)(url, LanguagePair.JA_KO)

            assertEquals(listOf("번역:첫 문단.", null), page.segments.map { it.translated })
            assertEquals(1, cache.puts.size)
        }

    @Test
    fun `수집 실패는 그대로 던져 화면이 알리게 한다`() =
        runTest {
            val source = FakeWebPageSource(error = IllegalStateException("페이지를 가져오지 못했어요."))

            val error = runCatching { useCase(source = source)(url, LanguagePair.JA_KO) }.exceptionOrNull()

            assertEquals("페이지를 가져오지 못했어요.", error?.message)
        }

    @Test
    fun `본문이 비면 세그먼트가 없다`() =
        runTest {
            val source = FakeWebPageSource(WebPage(url, "빈 페이지", "   "))

            val page = useCase(source = source)(url, LanguagePair.JA_KO)

            assertTrue(page.segments.isEmpty())
        }

    @Test
    fun `단건 재번역은 결과를 캐시에 넣는다`() =
        runTest {
            val engine = FakeTranslationEngine()

            val result = TranslateSegmentUseCase(engine, cache)(Segment("seg-1", "原文"), LanguagePair.JA_KO)

            assertEquals("번역:原文", result.translated)
            assertEquals(1, cache.puts.size)
        }

    @Test
    fun `단건 재번역이 실패하면 번역문 없이 돌려준다`() =
        runTest {
            val engine = FakeTranslationEngine(error = IllegalStateException("실패"))

            val result = TranslateSegmentUseCase(engine, cache)(Segment("seg-1", "原文"), LanguagePair.JA_KO)

            assertNull(result.translated)
            assertTrue(cache.puts.isEmpty())
        }
}
