package com.android.offread.translate.domain.usecase

import com.android.offread.core.entity.LanguagePair
import com.android.offread.translate.domain.ChapterTextSource
import com.android.offread.translate.domain.FakeGlossaryProvider
import com.android.offread.translate.domain.FakeSegmentCache
import com.android.offread.translate.domain.FakeTermSuggestionSink
import com.android.offread.translate.domain.FakeTranslationEngine
import com.android.offread.translate.domain.GlossaryPostProcessor
import com.android.offread.translate.domain.SegmentSplitter
import com.android.offread.translate.domain.TermCandidateExtractor
import com.android.offread.translate.domain.model.SegmentCacheKey
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeChapterTextSource(
    private val body: String,
) : ChapterTextSource {
    override suspend fun text(
        itemId: String,
        chapterIndex: Int,
    ): String = body

    override fun title(chapterIndex: Int): String = "${chapterIndex}화"
}

class PretranslateChapterUseCaseTest {
    private val cache = FakeSegmentCache()
    private val body = "첫 문단.\n\n둘째 문단."

    private fun useCase(engine: FakeTranslationEngine) =
        PretranslateChapterUseCase(
            FakeChapterTextSource(body),
            SegmentSplitter(),
            TranslateChapterUseCase(
                engine,
                cache,
                FakeGlossaryProvider(),
                GlossaryPostProcessor(),
                SuggestTermsUseCase(TermCandidateExtractor(), engine, FakeTermSuggestionSink()),
            ),
        )

    @Test
    fun `원문을 문단으로 쪼개 번역하고 캐시를 채운다`() =
        runTest {
            val engine = FakeTranslationEngine()

            val result = useCase(engine)("i1", "c1", chapterIndex = 7, languagePair = LanguagePair.JA_KO)

            assertEquals(listOf("번역:첫 문단.", "번역:둘째 문단."), result.map { it.translated })
            assertEquals(2, cache.puts.size)
        }

    @Test
    fun `이미 캐시된 화는 추론하지 않는다`() =
        runTest {
            cache.seed(SegmentCacheKey.of("첫 문단.", "c1", "v1"), "캐시 1")
            cache.seed(SegmentCacheKey.of("둘째 문단.", "c1", "v1"), "캐시 2")
            val engine = FakeTranslationEngine()

            val result = useCase(engine)("i1", "c1", chapterIndex = 7, languagePair = LanguagePair.JA_KO)

            assertTrue(result.all { it.fromCache })
            assertTrue(engine.translatedTexts.isEmpty())
        }
}
