package com.android.offread.translate.data

import com.android.offread.core.entity.LanguagePair
import com.android.offread.translate.domain.TranslationEngine
import com.android.offread.translate.domain.TranslationEnginePreference
import com.android.offread.translate.domain.model.GlossaryEntry
import com.android.offread.translate.domain.model.TranslationEngineKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

private class FakePreference(
    initial: TranslationEngineKind = TranslationEngineKind.ML_KIT,
) : TranslationEnginePreference {
    private val state = MutableStateFlow(initial)

    override val selected: Flow<TranslationEngineKind> = state

    override suspend fun select(kind: TranslationEngineKind) {
        state.value = kind
    }
}

private class NamedEngine(
    private val name: String,
) : TranslationEngine {
    override suspend fun translate(
        text: String,
        pair: LanguagePair,
        glossary: List<GlossaryEntry>,
    ): String = "$name:$text"

    override suspend fun modelVersion(pair: LanguagePair): String = "$name-v1"
}

class SwitchingTranslationEngineTest {
    private val preference = FakePreference()
    private val engine =
        SwitchingTranslationEngine(preference, NamedEngine("mlkit"), NamedEngine("llm"))

    @Test
    fun `기본값은 ML Kit 이다`() =
        runTest {
            assertEquals("mlkit:原文", engine.translate("原文", LanguagePair.JA_KO, emptyList()))
        }

    @Test
    fun `설정을 바꾸면 다음 번역부터 새 엔진을 쓴다`() =
        runTest {
            preference.select(TranslationEngineKind.ON_DEVICE_LLM)

            assertEquals("llm:原文", engine.translate("原文", LanguagePair.JA_KO, emptyList()))
        }

    @Test
    fun `엔진마다 모델 버전이 달라 캐시가 섞이지 않는다`() =
        runTest {
            val mlKitVersion = engine.modelVersion(LanguagePair.JA_KO)
            preference.select(TranslationEngineKind.ON_DEVICE_LLM)
            val llmVersion = engine.modelVersion(LanguagePair.JA_KO)

            assertNotEquals(mlKitVersion, llmVersion)
        }

    @Test
    fun `LLM 만 프롬프트 용어 주입을 지원한다`() {
        assertEquals(false, TranslationEngineKind.ML_KIT.supportsGlossaryPrompt)
        assertEquals(true, TranslationEngineKind.ON_DEVICE_LLM.supportsGlossaryPrompt)
    }
}
