package com.android.offread.importer.domain

import com.android.offread.importer.domain.model.WebNovelMetadata

/**
 * 웹소설 사이트 인식·메타 추출 포트(헥사고날). 지원 사이트 화이트리스트(P-02)와 실제 스크래핑은
 * 어댑터(import:data)에 감춘다.
 */
interface WebNovelImporter {
    /** 지원 사이트 화이트리스트에 속하는 URL 인가. */
    fun isSupported(url: String): Boolean

    /** URL 에서 작품 메타를 추출한다. */
    suspend fun recognize(url: String): WebNovelMetadata
}
