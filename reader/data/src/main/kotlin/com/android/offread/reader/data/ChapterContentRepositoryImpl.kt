package com.android.offread.reader.data

import com.android.offread.core.entity.LanguagePair
import com.android.offread.library.domain.LibraryRepository
import com.android.offread.reader.domain.ChapterContentRepository
import com.android.offread.reader.domain.model.ChapterContent
import com.android.offread.reader.domain.model.ReaderSegment
import com.android.offread.translate.domain.SegmentSplitter
import com.android.offread.translate.domain.model.TranslationRequest
import com.android.offread.translate.domain.usecase.TranslateChapterUseCase
import com.android.offread.translate.domain.usecase.TranslateSegmentUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * [ChapterContentRepository] 어댑터(F-015). 원문을 세그먼트로 쪼개 번역 파이프라인(F-020)에 태운다.
 * 캐시가 있으면 추론 없이 즉시 돌아온다(F-021).
 *
 * 원문 수집은 아직 스텁이다([StubChapterSource]) — 실제 수집은 F-012 인프라 태스크.
 */
class ChapterContentRepositoryImpl
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
        private val splitter: SegmentSplitter,
        private val translateChapter: TranslateChapterUseCase,
        private val translateSegment: TranslateSegmentUseCase,
    ) : ChapterContentRepository {
        override suspend fun getChapter(
            itemId: String,
            chapterIndex: Int,
        ): ChapterContent {
            val collectionId = collectionIdOf(itemId)
            val segments = splitter.split(StubChapterSource.text(chapterIndex))
            val translated =
                translateChapter(
                    TranslationRequest(
                        itemId = itemId,
                        collectionId = collectionId,
                        chapterIndex = chapterIndex,
                        languagePair = languagePairOf(),
                        segments = segments,
                    ),
                )
            return ChapterContent(
                itemId = itemId,
                chapterIndex = chapterIndex,
                title = StubChapterSource.title(chapterIndex),
                segments = translated.map { ReaderSegment(it.id, it.original, it.translated) },
            )
        }

        override suspend fun retrySegment(
            itemId: String,
            chapterIndex: Int,
            segmentId: String,
        ): String {
            val segment =
                splitter
                    .split(StubChapterSource.text(chapterIndex))
                    .firstOrNull { it.id == segmentId }
                    ?: error("다시 시도할 문단을 찾지 못했어요.")
            val result =
                translateSegment(
                    itemId = itemId,
                    collectionId = collectionIdOf(itemId),
                    chapterIndex = chapterIndex,
                    languagePair = languagePairOf(),
                    segment = segment,
                )
            return result.translated ?: error("번역에 실패했어요. 잠시 후 다시 시도해 주세요.")
        }

        private suspend fun collectionIdOf(itemId: String): String =
            libraryRepository.observeItem(itemId).first()?.collectionId
                ?: error("작품을 찾을 수 없어요.")

        /**
         * 원문 언어는 아직 수집 단계에서 확정되지 않는다(F-012 인프라). 스텁 원문이 일본어라
         * 일→한 고정이며, 실제 수집이 붙으면 아이템 메타에서 결정한다.
         */
        private fun languagePairOf(): LanguagePair = LanguagePair.JA_KO
    }
