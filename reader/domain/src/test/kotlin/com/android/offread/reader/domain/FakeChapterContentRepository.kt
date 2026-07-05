package com.android.offread.reader.domain

import com.android.offread.reader.domain.model.ChapterContent
import com.android.offread.reader.domain.model.ReaderSegment

/** 결정적 [ChapterContentRepository] 더블. 세그먼트 s2 를 미번역으로 두고 retry 로 번역시킨다. */
class FakeChapterContentRepository : ChapterContentRepository {
    override suspend fun getChapter(
        itemId: String,
        chapterIndex: Int,
    ): ChapterContent =
        ChapterContent(
            itemId = itemId,
            chapterIndex = chapterIndex,
            title = "${chapterIndex}화",
            segments =
                listOf(
                    ReaderSegment("s1", "原文1", "번역1"),
                    ReaderSegment("s2", "原文2", null),
                ),
        )

    override suspend fun retrySegment(
        itemId: String,
        chapterIndex: Int,
        segmentId: String,
    ): String = "재번역-$segmentId"
}
