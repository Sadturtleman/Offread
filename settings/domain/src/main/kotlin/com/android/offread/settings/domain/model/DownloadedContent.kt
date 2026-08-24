package com.android.offread.settings.domain.model

import com.android.offread.library.domain.model.LibraryItem

/**
 * S-05 저장·캐시 관리 화면의 한 줄(F-031). 어떤 작품이 저장 공간을 쓰고 있는지 보여준다.
 *
 * @property cachedSegments 이 작품이 캐시에 갖고 있는 세그먼트 수
 * @property bytes 이 작품의 번역 캐시 용량
 */
data class DownloadedContent(
    val item: LibraryItem,
    val cachedSegments: Int,
    val bytes: Long,
)
