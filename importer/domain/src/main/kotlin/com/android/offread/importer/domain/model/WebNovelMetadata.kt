package com.android.offread.importer.domain.model

import com.android.offread.core.entity.SerialStatus

/** 웹소설 URL 에서 인식·추출한 작품 메타데이터(F-012). */
data class WebNovelMetadata(
    val siteName: String,
    val title: String,
    val author: String,
    val totalChapters: Int,
    val serialStatus: SerialStatus,
    val sourceUrl: String,
)
