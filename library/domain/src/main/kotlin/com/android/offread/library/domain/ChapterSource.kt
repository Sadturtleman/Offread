package com.android.offread.library.domain

/**
 * 원본 사이트에서 연재 상태를 조회하는 포트(헥사고날, F-013).
 * 실제 HTTP 수집은 어댑터에 감춘다. 자동 백그라운드 체크·알림은 후속 범위다.
 */
interface ChapterSource {
    /** 현재 공개된 총 화수. 미지원 사이트·네트워크 실패는 예외로 알린다. */
    suspend fun fetchTotalChapters(sourceUrl: String): Int
}
