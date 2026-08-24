package com.android.offread.translate.domain.usecase

import com.android.offread.core.entity.LanguagePair
import com.android.offread.translate.domain.ChapterTextSource
import com.android.offread.translate.domain.SegmentSplitter
import com.android.offread.translate.domain.model.TranslatedSegment
import com.android.offread.translate.domain.model.TranslationRequest
import javax.inject.Inject

/**
 * F-022: 한 화를 미리 번역해 캐시에 채운다. 리더가 나중에 그 화를 열면 캐시 히트로 즉시 뜬다(F-021).
 * 이미 캐시된 화는 파이프라인이 알아서 추론을 건너뛰므로 여기서 따로 검사하지 않는다.
 */
class PretranslateChapterUseCase
    @Inject
    constructor(
        private val chapterTextSource: ChapterTextSource,
        private val splitter: SegmentSplitter,
        private val translateChapter: TranslateChapterUseCase,
    ) {
        suspend operator fun invoke(
            itemId: String,
            collectionId: String,
            chapterIndex: Int,
            languagePair: LanguagePair,
        ): List<TranslatedSegment> {
            val segments = splitter.split(chapterTextSource.text(itemId, chapterIndex))
            return translateChapter(
                TranslationRequest(
                    itemId = itemId,
                    collectionId = collectionId,
                    chapterIndex = chapterIndex,
                    languagePair = languagePair,
                    segments = segments,
                ),
            )
        }
    }
