package com.android.offread.reader.domain.usecase

import com.android.offread.library.domain.LibraryRepository
import com.android.offread.reader.domain.ChapterContentRepository
import com.android.offread.reader.domain.model.ChapterContent
import javax.inject.Inject

/** F-015: 특정 화 본문을 가져온다. chapterIndex 는 1..totalChapters 로 클램프한다. */
class GetChapterContentUseCase
    @Inject
    constructor(
        private val chapterContentRepository: ChapterContentRepository,
    ) {
        suspend operator fun invoke(
            itemId: String,
            chapterIndex: Int,
        ): ChapterContent = chapterContentRepository.getChapter(itemId, chapterIndex.coerceAtLeast(1))
    }

/** F-015 / P-08: 미번역 세그먼트 인라인 재시도. */
class RetrySegmentUseCase
    @Inject
    constructor(
        private val chapterContentRepository: ChapterContentRepository,
    ) {
        suspend operator fun invoke(
            itemId: String,
            chapterIndex: Int,
            segmentId: String,
        ): String = chapterContentRepository.retrySegment(itemId, chapterIndex, segmentId)
    }

/** F-015: 읽던 위치 저장. */
class SaveReadingProgressUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
    ) {
        suspend operator fun invoke(
            itemId: String,
            chapterIndex: Int,
        ) = libraryRepository.updateReadingProgress(itemId, chapterIndex)
    }
