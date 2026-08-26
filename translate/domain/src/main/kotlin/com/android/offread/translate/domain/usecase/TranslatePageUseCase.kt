package com.android.offread.translate.domain.usecase

import com.android.offread.core.entity.LanguagePair
import com.android.offread.translate.domain.SegmentCache
import com.android.offread.translate.domain.SegmentSplitter
import com.android.offread.translate.domain.TranslationEngine
import com.android.offread.translate.domain.WebPageSource
import com.android.offread.translate.domain.model.Segment
import com.android.offread.translate.domain.model.SegmentCacheKey
import com.android.offread.translate.domain.model.TranslatedPage
import com.android.offread.translate.domain.model.TranslatedSegment
import javax.inject.Inject

/**
 * MVP 본체: URL 을 받아 페이지를 수집하고 문단 단위로 번역한다.
 *
 * 캐시가 히트하면 추론을 건너뛰고, 세그먼트 하나가 실패해도 나머지는 계속 번역한다 —
 * 문단 하나 때문에 페이지 전체가 막히지 않게 한다.
 */
class TranslatePageUseCase
    @Inject
    constructor(
        private val webPageSource: WebPageSource,
        private val splitter: SegmentSplitter,
        private val engine: TranslationEngine,
        private val cache: SegmentCache,
    ) {
        suspend operator fun invoke(
            url: String,
            pair: LanguagePair,
        ): TranslatedPage {
            val page = webPageSource.fetch(url)
            val modelVersion = engine.modelVersion(pair)
            val segments =
                splitter.split(page.text).map { segment -> translateSegment(segment, pair, modelVersion) }
            return TranslatedPage(url = page.url, title = page.title, languagePair = pair, segments = segments)
        }

        private suspend fun translateSegment(
            segment: Segment,
            pair: LanguagePair,
            modelVersion: String,
        ): TranslatedSegment {
            val key = SegmentCacheKey.of(segment.original, modelVersion)
            cache.get(key)?.let { cached ->
                return TranslatedSegment(segment.id, segment.original, cached, fromCache = true)
            }
            val translated =
                runCatching { engine.translate(segment.original, pair) }.getOrNull()
                    ?: return TranslatedSegment(segment.id, segment.original, translated = null)
            cache.put(key, translated)
            return TranslatedSegment(segment.id, segment.original, translated)
        }
    }

/** 화면에서 실패한 문단 하나만 다시 번역한다. */
class TranslateSegmentUseCase
    @Inject
    constructor(
        private val engine: TranslationEngine,
        private val cache: SegmentCache,
    ) {
        suspend operator fun invoke(
            segment: Segment,
            pair: LanguagePair,
        ): TranslatedSegment {
            val modelVersion = engine.modelVersion(pair)
            val key = SegmentCacheKey.of(segment.original, modelVersion)
            val translated =
                runCatching { engine.translate(segment.original, pair) }.getOrElse {
                    return TranslatedSegment(segment.id, segment.original, translated = null)
                }
            cache.put(key, translated)
            return TranslatedSegment(segment.id, segment.original, translated)
        }
    }
