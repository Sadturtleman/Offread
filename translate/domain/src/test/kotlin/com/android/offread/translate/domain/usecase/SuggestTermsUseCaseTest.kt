package com.android.offread.translate.domain.usecase

import com.android.offread.core.entity.LanguagePair
import com.android.offread.translate.domain.FakeTermSuggestionSink
import com.android.offread.translate.domain.FakeTranslationEngine
import com.android.offread.translate.domain.TermCandidateExtractor
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SuggestTermsUseCaseTest {
    private val originals = List(2) { "ルーデウスは分かれ道の前で立ち止まった。" }

    private fun useCase(
        sink: FakeTermSuggestionSink,
        engine: FakeTranslationEngine = FakeTranslationEngine(),
    ) = SuggestTermsUseCase(TermCandidateExtractor(), engine, sink)

    @Test
    fun `후보를 단독 번역해 캐논값과 함께 제안한다`() =
        runTest {
            val sink = FakeTermSuggestionSink()
            val engine = FakeTranslationEngine(translation = { "루데우스" })

            val suggested = useCase(sink, engine)("c1", LanguagePair.JA_KO, originals)

            assertEquals(listOf("ルーデウス"), suggested.map { it.source })
            assertEquals(listOf("ルーデウス" to "루데우스"), sink.suggestions.map { it.source to it.translation })
            assertEquals(2, sink.suggestions.single().occurrenceCount)
        }

    @Test
    fun `이미 있는 용어는 제안하지 않는다`() =
        runTest {
            val sink = FakeTermSuggestionSink(existing = setOf("ルーデウス"))

            val suggested = useCase(sink)("c1", LanguagePair.JA_KO, originals)

            assertTrue(suggested.isEmpty())
            assertTrue(sink.suggestions.isEmpty())
        }

    @Test
    fun `후보 번역에 실패하면 그 후보만 건너뛴다`() =
        runTest {
            val sink = FakeTermSuggestionSink()
            val engine = FakeTranslationEngine(error = IllegalStateException("모델 미설치"))

            val suggested = useCase(sink, engine)("c1", LanguagePair.JA_KO, originals)

            assertTrue(suggested.isEmpty())
            assertTrue(sink.suggestions.isEmpty())
        }
}
