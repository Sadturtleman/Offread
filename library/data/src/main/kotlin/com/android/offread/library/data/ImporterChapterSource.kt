package com.android.offread.library.data

import com.android.offread.importer.domain.WebNovelImporter
import com.android.offread.library.domain.ChapterSource
import javax.inject.Inject

/**
 * [ChapterSource] 어댑터(F-013). 가져오기와 같은 수집 경로를 재사용해 현재 화수를 읽는다.
 * 실제 HTTP 수집·파싱은 [WebNovelImporter] 어댑터 교체(F-012 인프라)로 채워진다.
 */
class ImporterChapterSource
    @Inject
    constructor(
        private val importer: WebNovelImporter,
    ) : ChapterSource {
        override suspend fun fetchTotalChapters(sourceUrl: String): Int {
            require(importer.isSupported(sourceUrl)) { "지원하지 않는 사이트예요." }
            return importer.recognize(sourceUrl).totalChapters
        }
    }
