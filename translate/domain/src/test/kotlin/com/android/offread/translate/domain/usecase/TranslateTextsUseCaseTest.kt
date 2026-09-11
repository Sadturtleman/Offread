package com.android.offread.translate.domain.usecase

import com.android.offread.core.entity.LanguagePair
import com.android.offread.translate.domain.FakeSegmentCache
import com.android.offread.translate.domain.FakeTranslationEngine
import com.android.offread.translate.domain.SegmentSplitter
import com.android.offread.translate.domain.TranslationEngine
import com.android.offread.translate.domain.TranslationEngineUnavailableException
import com.android.offread.translate.domain.model.SegmentCacheKey
import com.android.offread.translate.domain.model.VisibleText
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslateTextsUseCaseTest {
    private val cache = FakeSegmentCache()

    private fun useCase(engine: TranslationEngine = FakeTranslationEngine()) = TranslateTextsUseCase(SegmentSplitter(), engine, cache)

    @Test
    fun `노드마다 번역문을 하나씩 흘려보낸다`() =
        runTest {
            val results = useCase()(listOf(VisibleText("0", "첫 노드."), VisibleText("1", "둘째 노드.")), LanguagePair.JA_KO).toList()

            assertEquals(listOf("0", "1"), results.map { it.id })
            assertEquals(listOf("번역:첫 노드.", "번역:둘째 노드."), results.map { it.translated })
            assertEquals(2, cache.puts.size)
        }

    @Test
    fun `캐시가 히트하면 추론을 건너뛴다`() =
        runTest {
            cache.seed(SegmentCacheKey.of("첫 노드.", "v1"), "캐시된 번역")
            val engine = FakeTranslationEngine()

            val results = useCase(engine)(listOf(VisibleText("0", "첫 노드.")), LanguagePair.JA_KO).toList()

            assertEquals("캐시된 번역", results.single().translated)
            assertTrue(engine.translatedTexts.isEmpty())
        }

    @Test
    fun `긴 노드는 문장 경계에서 쪼개 번역하고 다시 잇는다`() =
        runTest {
            val long = "あ".repeat(500) + "。" + "い".repeat(100) + "。"
            val engine = FakeTranslationEngine()

            val results = useCase(engine)(listOf(VisibleText("0", long)), LanguagePair.JA_KO).toList()

            assertEquals(2, engine.translatedTexts.size)
            assertTrue(results.single().translated!!.contains(" "))
        }

    @Test
    fun `노드 하나가 실패해도 나머지는 계속 번역한다`() =
        runTest {
            val engine =
                object : TranslationEngine {
                    override suspend fun translate(
                        text: String,
                        pair: LanguagePair,
                    ): String {
                        if (text == "둘째 노드.") throw IllegalStateException("추론 실패")
                        return "번역:$text"
                    }

                    override suspend fun modelVersion(pair: LanguagePair): String = "v1"
                }

            val results =
                useCase(engine)(listOf(VisibleText("0", "첫 노드."), VisibleText("1", "둘째 노드.")), LanguagePair.JA_KO).toList()

            assertEquals("번역:첫 노드.", results.first().translated)
            assertNull(results.last().translated)
        }

    @Test
    fun `엔진이 준비되지 않으면 그대로 던져 화면이 알리게 한다`() =
        runTest {
            val engine = FakeTranslationEngine(error = TranslationEngineUnavailableException("모델 파일이 없어요."))

            val error =
                runCatching {
                    useCase(engine)(listOf(VisibleText("0", "첫 노드.")), LanguagePair.JA_KO).toList()
                }.exceptionOrNull()

            assertEquals("모델 파일이 없어요.", error?.message)
        }
}
