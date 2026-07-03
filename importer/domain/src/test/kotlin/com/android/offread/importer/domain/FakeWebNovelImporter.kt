package com.android.offread.importer.domain

import com.android.offread.core.entity.SerialStatus
import com.android.offread.importer.domain.model.WebNovelMetadata

/** 화이트리스트·메타를 제어하는 [WebNovelImporter] 더블. */
class FakeWebNovelImporter(
    private val supportedHosts: Set<String> = setOf("ncode.syosetu.com", "kakuyomu.jp"),
) : WebNovelImporter {
    override fun isSupported(url: String): Boolean = supportedHosts.any { url.contains(it) }

    override suspend fun recognize(url: String): WebNovelMetadata =
        WebNovelMetadata(
            siteName = "소설가가 되자",
            title = "무직전생",
            author = "理不尽な孫の手",
            totalChapters = 286,
            serialStatus = SerialStatus.ONGOING,
            sourceUrl = url,
        )
}
