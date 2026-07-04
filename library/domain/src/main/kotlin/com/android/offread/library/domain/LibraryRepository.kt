package com.android.offread.library.domain

import com.android.offread.core.entity.TranslationStatus
import com.android.offread.library.domain.model.Collection
import com.android.offread.library.domain.model.LibraryItem
import com.android.offread.library.domain.model.LibrarySort
import kotlinx.coroutines.flow.Flow

/**
 * 라이브러리 저장 포트(헥사고날). 구현체는 Room 어댑터(library:data). 서버 없음(P-01).
 */
interface LibraryRepository {
    fun observeCollections(sort: LibrarySort): Flow<List<Collection>>

    /** 컬렉션 생성. 생성된 id 를 반환한다. */
    suspend fun createCollection(
        name: String,
        parentId: String?,
    ): String

    suspend fun renameCollection(
        id: String,
        name: String,
    )

    /** 삭제(즉시, 휴지통 없음). 하위 컬렉션·아이템·용어·캐시가 연쇄 삭제된다(P-04). */
    suspend fun deleteCollection(id: String)

    /** 전체 아이템(최근 갱신 순). collectionId 가 있으면 해당 컬렉션만. */
    fun observeItems(collectionId: String? = null): Flow<List<LibraryItem>>

    /** 단일 아이템 구독(없으면 null). */
    fun observeItem(id: String): Flow<LibraryItem?>

    /** 아이템 추가. 생성된 id 반환. */
    suspend fun addItem(item: LibraryItem): String

    /** 아이템 번역 상태 갱신(F-019 배지 전이). */
    suspend fun updateItemTranslationStatus(
        id: String,
        status: TranslationStatus,
    )

    /** 읽던 위치 저장(F-015). */
    suspend fun updateReadingProgress(
        id: String,
        lastReadChapter: Int,
    )
}
