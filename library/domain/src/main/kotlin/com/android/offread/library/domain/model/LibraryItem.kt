package com.android.offread.library.domain.model

import com.android.offread.core.entity.ItemType
import com.android.offread.core.entity.SerialStatus
import com.android.offread.core.entity.TranslationStatus

/**
 * 라이브러리 아이템(웹소설 작품/논문 문서). 원문은 개인 열람용으로만 기기 내 저장(P-02).
 *
 * @property collectionId 소속 컬렉션
 * @property totalChapters 웹소설 화수(논문은 0)
 */
data class LibraryItem(
    val id: String,
    val collectionId: String,
    val type: ItemType,
    val title: String,
    val author: String,
    val sourceUrl: String,
    val siteName: String,
    val totalChapters: Int,
    val serialStatus: SerialStatus,
    val translationStatus: TranslationStatus,
    val updatedAt: Long,
)
