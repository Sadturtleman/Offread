package com.android.offread.translate.domain.usecase

import com.android.offread.core.entity.LanguagePair
import com.android.offread.translate.domain.FakeGlossaryProvider
import com.android.offread.translate.domain.FakeSegmentCache
import com.android.offread.translate.domain.FakeTermSuggestionSink
import com.android.offread.translate.domain.FakeTranslationEngine
import com.android.offread.translate.domain.GlossaryPostProcessor
import com.android.offread.translate.domain.TermCandidateExtractor
import com.android.offread.translate.domain.model.GlossaryEntry
import com.android.offread.translate.domain.model.Segment
import com.android.offread.translate.domain.model.SegmentCacheKey
import com.android.offread.translate.domain.model.TranslationRequest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslateChapterUseCaseTest {
    private val pair = LanguagePair.JA_KO
    private val cache = FakeSegmentCache()

    private fun request(
        vararg originals: String,
        collectionId: String = "c1",
    ) = TranslationRequest(
        itemId = "i1",
        collectionId = collectionId,
        chapterIndex = 1,
        languagePair = pair,
        segments = originals.mapIndexed { index, text -> Segment("seg-${index + 1}", text) },
    )

    private val sink = FakeTermSuggestionSink()

    private fun useCase(
        engine: FakeTranslationEngine = FakeTranslationEngine(),
        glossary: List<GlossaryEntry> = emptyList(),
    ) = TranslateChapterUseCase(
        engine,
        cache,
        FakeGlossaryProvider(glossary),
        GlossaryPostProcessor(),
        SuggestTermsUseCase(TermCandidateExtractor(), engine, sink),
    )

    @Test
    fun `캐시가 비어 있으면 추론하고 결과를 캐시에 넣는다`() =
        runTest {
            val engine = FakeTranslationEngine()

            val result = useCase(engine)(request("原文1", "原文2"))

            assertEquals(listOf("번역:原文1", "번역:原文2"), result.map { it.translated })
            assertTrue(result.none { it.fromCache })
            assertEquals(listOf("原文1", "原文2"), engine.translatedTexts)
            assertEquals(2, cache.puts.size)
        }

    @Test
    fun `캐시가 히트하면 추론을 건너뛴다`() =
        runTest {
            cache.seed(SegmentCacheKey.of("原文1", "c1", "v1"), "캐시된 번역")
            val engine = FakeTranslationEngine()

            val result = useCase(engine)(request("原文1"))

            assertEquals("캐시된 번역", result.single().translated)
            assertTrue(result.single().fromCache)
            assertTrue(engine.translatedTexts.isEmpty())
        }

    @Test
    fun `다른 컬렉션의 캐시는 재사용하지 않는다`() =
        runTest {
            cache.seed(SegmentCacheKey.of("原文1", "c1", "v1"), "c1 번역")
            val engine = FakeTranslationEngine()

            val result = useCase(engine)(request("原文1", collectionId = "c2"))

            assertEquals("번역:原文1", result.single().translated)
            assertFalse(result.single().fromCache)
        }

    @Test
    fun `모델 버전이 바뀌면 옛 캐시를 쓰지 않는다`() =
        runTest {
            cache.seed(SegmentCacheKey.of("原文1", "c1", "v1"), "옛 번역")
            val engine = FakeTranslationEngine(version = "v2")

            val result = useCase(engine)(request("原文1"))

            assertEquals("번역:原文1", result.single().translated)
        }

    @Test
    fun `용어맵을 엔진에 주입하고 고정 용어는 후처리로 강제한다`() =
        runTest {
            val glossary = listOf(GlossaryEntry("ルーデウス", "루데우스", pinned = true))
            val engine = FakeTranslationEngine(translation = { "ルーデウス가 걸었다." })

            val result = useCase(engine, glossary)(request("原文"))

            assertEquals(glossary, engine.lastGlossary)
            assertEquals("루데우스가 걸었다.", result.single().translated)
        }

    @Test
    fun `추론에 실패한 세그먼트는 번역문 없이 돌려주고 캐시하지 않는다`() =
        runTest {
            val engine = FakeTranslationEngine(error = IllegalStateException("모델 미설치"))

            val result = useCase(engine)(request("原文1"))

            assertNull(result.single().translated)
            assertTrue(cache.puts.isEmpty())
        }

    @Test
    fun `새로 번역한 원문에서 용어 후보를 제안한다`() =
        runTest {
            useCase()(
                request(
                    "ルーデウスは分かれ道の前で立ち止まった。",
                    "ルーデウスは地図を畳んだ。",
                ),
            )

            assertEquals(listOf("ルーデウス"), sink.suggestions.map { it.source })
            assertEquals("c1", sink.suggestions.single().collectionId)
        }

    @Test
    fun `전부 캐시 히트면 이미 제안이 끝난 범위라 다시 제안하지 않는다`() =
        runTest {
            val first = "ルーデウスは分かれ道の前で立ち止まった。"
            val second = "ルーデウスは地図を畳んだ。"
            cache.seed(SegmentCacheKey.of(first, "c1", "v1"), "캐시 1")
            cache.seed(SegmentCacheKey.of(second, "c1", "v1"), "캐시 2")

            useCase()(request(first, second))

            assertTrue(sink.suggestions.isEmpty())
        }

    @Test
    fun `세그먼트 하나가 실패해도 나머지는 계속 번역한다`() =
        runTest {
            var calls = 0
            val engine =
                object : com.android.offread.translate.domain.TranslationEngine {
                    override suspend fun translate(
                        text: String,
                        pair: LanguagePair,
                        glossary: List<GlossaryEntry>,
                    ): String {
                        calls++
                        if (text == "原文2") throw IllegalStateException("일시 실패")
                        return "번역:$text"
                    }

                    override suspend fun modelVersion(pair: LanguagePair): String = "v1"
                }
            val useCase =
                TranslateChapterUseCase(
                    engine,
                    cache,
                    FakeGlossaryProvider(),
                    GlossaryPostProcessor(),
                    SuggestTermsUseCase(TermCandidateExtractor(), engine, sink),
                )

            val result = useCase(request("原文1", "原文2", "原文3"))

            assertEquals(listOf("번역:原文1", null, "번역:原文3"), result.map { it.translated })
            assertEquals(3, calls)
        }
}

class TranslateSegmentUseCaseTest {
    @Test
    fun `단건 재번역도 같은 파이프라인을 탄다`() =
        runTest {
            val cache = FakeSegmentCache()
            val engine = FakeTranslationEngine()
            val chapter =
                TranslateChapterUseCase(
                    engine,
                    cache,
                    FakeGlossaryProvider(),
                    GlossaryPostProcessor(),
                    SuggestTermsUseCase(
                        TermCandidateExtractor(),
                        engine,
                        com.android.offread.translate.domain
                            .FakeTermSuggestionSink(),
                    ),
                )

            val result =
                TranslateSegmentUseCase(chapter)(
                    itemId = "i1",
                    collectionId = "c1",
                    chapterIndex = 3,
                    languagePair = LanguagePair.JA_KO,
                    segment = Segment("seg-9", "原文"),
                )

            assertEquals("번역:原文", result.translated)
            assertEquals("seg-9", result.id)
            assertEquals(1, cache.puts.size)
        }
}
