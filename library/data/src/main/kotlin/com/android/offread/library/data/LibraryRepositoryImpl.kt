package com.android.offread.library.data

import com.android.offread.core.database.CollectionDao
import com.android.offread.core.database.CollectionEntity
import com.android.offread.core.database.ItemDao
import com.android.offread.core.database.TermDao
import com.android.offread.core.entity.TranslationStatus
import com.android.offread.library.domain.LibraryRepository
import com.android.offread.library.domain.model.Collection
import com.android.offread.library.domain.model.LibraryItem
import com.android.offread.library.domain.model.LibrarySort
import com.android.offread.library.domain.model.SearchQuery
import com.android.offread.library.domain.model.TermMapMoveStrategy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

/**
 * [LibraryRepository] 의 Room 어댑터. 컬렉션 삭제 연쇄는 자기참조 FK(CASCADE)가 DB 레벨에서 처리한다.
 */
class LibraryRepositoryImpl
    @Inject
    constructor(
        private val collectionDao: CollectionDao,
        private val itemDao: ItemDao,
        private val termDao: TermDao,
    ) : LibraryRepository {
        override fun observeCollections(sort: LibrarySort): Flow<List<Collection>> {
            val source =
                when (sort) {
                    LibrarySort.RECENT -> collectionDao.observeByRecent()
                    LibrarySort.NAME -> collectionDao.observeByName()
                }
            return source.map { list -> list.map(CollectionEntity::toDomain) }
        }

        override suspend fun createCollection(
            name: String,
            parentId: String?,
        ): String {
            val id = UUID.randomUUID().toString()
            collectionDao.insert(
                CollectionEntity(
                    id = id,
                    name = name,
                    parentId = parentId,
                    itemCount = 0,
                    termCount = 0,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
            return id
        }

        override suspend fun renameCollection(
            id: String,
            name: String,
        ) {
            collectionDao.rename(id, name, System.currentTimeMillis())
        }

        override suspend fun deleteCollection(id: String) {
            collectionDao.delete(id)
        }

        override fun observeItems(collectionId: String?): Flow<List<LibraryItem>> {
            val source =
                if (collectionId == null) itemDao.observeAll() else itemDao.observeByCollection(collectionId)
            return source.map { list -> list.map { it.toDomain() } }
        }

        override fun searchItems(query: SearchQuery): Flow<List<LibraryItem>> =
            itemDao
                .search(query.text.escapeLike(), query.collectionId, query.type?.name)
                .map { list -> list.map { it.toDomain() } }

        override fun observeItem(id: String): Flow<LibraryItem?> = itemDao.observeById(id).map { it?.toDomain() }

        override suspend fun addItem(item: LibraryItem): String {
            val id = item.id.ifBlank { UUID.randomUUID().toString() }
            itemDao.insert(item.copy(id = id, updatedAt = System.currentTimeMillis()).toEntity())
            return id
        }

        override suspend fun moveItem(
            id: String,
            targetCollectionId: String,
            strategy: TermMapMoveStrategy,
        ) {
            val item = itemDao.observeById(id).first() ?: return
            val now = System.currentTimeMillis()
            when (strategy) {
                TermMapMoveStrategy.MOVE -> {
                    termDao.deleteDuplicatedBySource(item.collectionId, targetCollectionId)
                    termDao.moveAll(item.collectionId, targetCollectionId, now)
                }
                TermMapMoveStrategy.MERGE -> {
                    val targetSources =
                        termDao.getByCollection(targetCollectionId).map { it.source }.toSet()
                    termDao
                        .getByCollection(item.collectionId)
                        .filterNot { it.source in targetSources }
                        .forEach { term ->
                            termDao.upsert(
                                term.copy(
                                    id = UUID.randomUUID().toString(),
                                    collectionId = targetCollectionId,
                                    updatedAt = now,
                                ),
                            )
                        }
                }
                TermMapMoveStrategy.LEAVE -> Unit
            }
            itemDao.updateCollection(id, targetCollectionId, now)
        }

        override suspend fun updateItemTranslationStatus(
            id: String,
            status: TranslationStatus,
        ) {
            itemDao.updateTranslationStatus(id, status.name, System.currentTimeMillis())
        }

        override suspend fun updateReadingProgress(
            id: String,
            lastReadChapter: Int,
        ) {
            itemDao.updateReadingProgress(id, lastReadChapter, System.currentTimeMillis())
        }
    }

/** 사용자가 입력한 LIKE 와일드카드를 리터럴로 취급한다(ItemDao.search 의 ESCAPE 와 짝). */
private fun String.escapeLike(): String =
    replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_")
