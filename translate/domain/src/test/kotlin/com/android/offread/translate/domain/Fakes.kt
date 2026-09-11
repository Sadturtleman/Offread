package com.android.offread.translate.domain

import com.android.offread.core.entity.LanguagePair
import com.android.offread.translate.domain.model.SegmentCacheKey

/** 인메모리 [SegmentCache] 더블. */
class FakeSegmentCache : SegmentCache {
    private val entries = mutableMapOf<SegmentCacheKey, String>()
    val puts = mutableListOf<SegmentCacheKey>()

    override suspend fun get(key: SegmentCacheKey): String? = entries[key]

    override suspend fun put(
        key: SegmentCacheKey,
        translation: String,
    ) {
        entries[key] = translation
        puts += key
    }

    override suspend fun stats(): CacheStats = CacheStats(entries.size, entries.values.sumOf { it.toByteArray().size.toLong() })

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

    override suspend fun translate(
        text: String,
        pair: LanguagePair,
    ): String {
        translatedTexts += text
        error?.let { throw it }
        return translation(text)
    }

    override suspend fun modelVersion(pair: LanguagePair): String = version
}
