package com.android.offread.translate.presentation

import com.android.offread.core.entity.LanguagePair
import com.android.offread.translate.domain.CacheStats
import com.android.offread.translate.domain.LlmModelFile
import com.android.offread.translate.domain.LlmModelStore
import com.android.offread.translate.domain.SegmentCache
import com.android.offread.translate.domain.TranslationEngine
import com.android.offread.translate.domain.TranslationEnginePreference
import com.android.offread.translate.domain.WebPageSource
import com.android.offread.translate.domain.model.SegmentCacheKey
import com.android.offread.translate.domain.model.TranslationEngineKind
import com.android.offread.translate.domain.model.WebPage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSegmentCache : SegmentCache {
    private val entries = mutableMapOf<SegmentCacheKey, String>()
    var cleared = false

    override suspend fun get(key: SegmentCacheKey): String? = entries[key]

    override suspend fun put(
        key: SegmentCacheKey,
        translation: String,
    ) {
        entries[key] = translation
    }

    override suspend fun stats(): CacheStats = CacheStats(entries.size, entries.size * 10L)

    override suspend fun clear() {
        cleared = true
        entries.clear()
    }
}

class FakeTranslationEngine(
    private val error: Throwable? = null,
) : TranslationEngine {
    override suspend fun translate(
        text: String,
        pair: LanguagePair,
    ): String = error?.let { throw it } ?: "번역:$text"

    override suspend fun modelVersion(pair: LanguagePair): String = "v1"
}

class FakeWebPageSource(
    private val page: WebPage? = null,
    private val error: Throwable? = null,
) : WebPageSource {
    override suspend fun fetch(url: String): WebPage {
        error?.let { throw it }
        return page ?: WebPage(url = url, title = "제목", text = "첫 문단.\n\n둘째 문단.")
    }
}

class FakeEnginePreference : TranslationEnginePreference {
    private val state = MutableStateFlow(TranslationEngineKind.ML_KIT)

    override val selected: Flow<TranslationEngineKind> = state

    override suspend fun select(kind: TranslationEngineKind) {
        state.value = kind
    }
}

class FakeLlmModelStore : LlmModelStore {
    val files = mutableListOf<LlmModelFile>()
    var importError: Throwable? = null

    override suspend fun installed(): List<LlmModelFile> = files.toList()

    override suspend fun import(uri: String): LlmModelFile {
        importError?.let { throw it }
        val file = LlmModelFile(uri.substringAfterLast('/'), 2_000_000_000)
        files += file
        return file
    }

    override suspend fun delete(name: String) {
        files.removeAll { it.name == name }
    }
}
