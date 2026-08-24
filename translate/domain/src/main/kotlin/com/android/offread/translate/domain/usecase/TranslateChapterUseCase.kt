package com.android.offread.translate.domain.usecase

import com.android.offread.core.entity.LanguagePair
import com.android.offread.translate.domain.GlossaryPostProcessor
import com.android.offread.translate.domain.GlossaryProvider
import com.android.offread.translate.domain.SegmentCache
import com.android.offread.translate.domain.TranslationEngine
import com.android.offread.translate.domain.model.GlossaryEntry
import com.android.offread.translate.domain.model.Segment
import com.android.offread.translate.domain.model.SegmentCacheKey
import com.android.offread.translate.domain.model.TranslatedSegment
import com.android.offread.translate.domain.model.TranslationRequest
import javax.inject.Inject

/**
 * F-020 번역 파이프라인: 컬렉션 용어맵 주입 → (캐시 조회) → 추론 → 후처리 → 캐시 저장.
 *
 * 캐시가 히트하면 추론을 아예 건너뛴다(F-021). 세그먼트 하나가 실패해도 나머지는 계속 진행하고,
 * 실패한 세그먼트는 번역문 없이 돌려줘 화면이 원문 + 재시도를 노출하게 한다(P-08).
 */
class TranslateChapterUseCase
    @Inject
    constructor(
        private val engine: TranslationEngine,
        private val cache: SegmentCache,
        private val glossaryProvider: GlossaryProvider,
        private val postProcessor: GlossaryPostProcessor,
    ) {
        suspend operator fun invoke(request: TranslationRequest): List<TranslatedSegment> {
            val glossary = glossaryProvider.glossaryFor(request.collectionId)
            val modelVersion = engine.modelVersion(request.languagePair)
            return request.segments.map { segment ->
                translateSegment(segment, request, glossary, modelVersion)
            }
        }

        private suspend fun translateSegment(
            segment: Segment,
            request: TranslationRequest,
            glossary: List<GlossaryEntry>,
            modelVersion: String,
        ): TranslatedSegment {
            val key = SegmentCacheKey.of(segment.original, request.collectionId, modelVersion)
            cache.get(key)?.let { cached ->
                return TranslatedSegment(segment.id, segment.original, cached, fromCache = true)
            }
            val translated =
                runCatching { engine.translate(segment.original, request.languagePair, glossary) }
                    .map { postProcessor.apply(it, glossary) }
                    .getOrNull()
                    ?: return TranslatedSegment(segment.id, segment.original, translated = null)

            cache.put(key, request.itemId, request.chapterIndex, translated)
            return TranslatedSegment(segment.id, segment.original, translated)
        }
    }

/**
 * F-020 / P-08: 세그먼트 1개 재번역(리더 인라인 재시도, 용어 편집 후 재번역).
 * 캐시를 우회하지 않고 같은 파이프라인을 그대로 한 조각에만 적용한다.
 */
class TranslateSegmentUseCase
    @Inject
    constructor(
        private val translateChapter: TranslateChapterUseCase,
    ) {
        suspend operator fun invoke(
            itemId: String,
            collectionId: String,
            chapterIndex: Int,
            languagePair: LanguagePair,
            segment: Segment,
        ): TranslatedSegment =
            translateChapter(
                TranslationRequest(
                    itemId = itemId,
                    collectionId = collectionId,
                    chapterIndex = chapterIndex,
                    languagePair = languagePair,
                    segments = listOf(segment),
                ),
            ).first()
    }
