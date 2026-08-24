package com.android.offread.translate.domain

import com.android.offread.core.entity.LanguagePair
import com.android.offread.translate.domain.model.GlossaryEntry
import com.android.offread.translate.domain.model.SegmentCacheKey

/** 인메모리 [SegmentCache] 더블. */
class FakeSegmentCache : SegmentCache {
    private val entries = mutableMapOf<SegmentCacheKey, String>()
    val puts = mutableListOf<SegmentCacheKey>()

    override suspend fun get(key: SegmentCacheKey): String? = entries[key]

    override suspend fun put(
        key: SegmentCacheKey,
        itemId: String,
        chapterIndex: Int,
        translation: String,
    ) {
        entries[key] = translation
        puts += key
    }

    override suspend fun invalidateItem(itemId: String) = entries.clear()

    override suspend fun invalidateChapter(
        itemId: String,
        chapterIndex: Int,
    ) = entries.clear()

    override suspend fun invalidateCollection(collectionId: String) {
        entries.keys.filter { it.collectionId == collectionId }.forEach { entries.remove(it) }
    }

    override suspend fun stats(): CacheStats = CacheStats(entries.size, entries.values.sumOf { it.toByteArray().size.toLong() })

    override suspend fun usageByItem(): Map<String, CacheStats> = emptyMap()

    override suspend fun clear() = entries.clear()

    fun seed(
        key: SegmentCacheKey,
        translation: String,
    ) {
        entries[key] = translation
    }
}

/** 호출을 기록하는 [TranslationEngine] 더블. */
class FakeTranslationEngine(
    private val version: String = "v1",
    private val error: Throwable? = null,
    private val translation: (String) -> String = { "번역:$it" },
) : TranslationEngine {
    val translatedTexts = mutableListOf<String>()
    var lastGlossary: List<GlossaryEntry> = emptyList()

    override suspend fun translate(
        text: String,
        pair: LanguagePair,
        glossary: List<GlossaryEntry>,
    ): String {
        translatedTexts += text
        lastGlossary = glossary
        error?.let { throw it }
        return translation(text)
    }

    override suspend fun modelVersion(pair: LanguagePair): String = version
}

/** 고정 용어 목록을 돌려주는 [GlossaryProvider] 더블. */
class FakeGlossaryProvider(
    private val glossary: List<GlossaryEntry> = emptyList(),
) : GlossaryProvider {
    override suspend fun glossaryFor(collectionId: String): List<GlossaryEntry> = glossary
}

/** 제안을 기록하는 [TermSuggestionSink] 더블. */
class FakeTermSuggestionSink(
    private val existing: Set<String> = emptySet(),
) : TermSuggestionSink {
    data class Suggestion(
        val collectionId: String,
        val source: String,
        val translation: String,
        val occurrenceCount: Int,
    )

    val suggestions = mutableListOf<Suggestion>()

    override suspend fun existingSources(collectionId: String): Set<String> = existing

    override suspend fun suggest(
        collectionId: String,
        source: String,
        translation: String,
        occurrenceCount: Int,
    ) {
        suggestions += Suggestion(collectionId, source, translation, occurrenceCount)
    }
}
