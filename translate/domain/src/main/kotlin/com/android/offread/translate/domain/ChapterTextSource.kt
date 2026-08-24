package com.android.offread.translate.domain

/**
 * 챕터 **원문** 조회 포트. 번역 코어는 원문을 어디서 어떻게 가져오는지 모른다 —
 * 리더가 화면에 띄울 때도, 선번역 큐가 백그라운드에서 미리 돌릴 때도 같은 원문을 쓴다.
 */
interface ChapterTextSource {
    suspend fun text(
        itemId: String,
        chapterIndex: Int,
    ): String

    fun title(chapterIndex: Int): String
}
