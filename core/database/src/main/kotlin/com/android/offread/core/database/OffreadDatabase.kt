package com.android.offread.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * 앱 로컬 DB(P-01 온디바이스). 라이브러리·컬렉션·아이템·용어맵·캐시 메타를 담는다.
 * 컬렉션·아이템·용어맵·세그먼트 캐시 테이블을 담는다.
 */
@Database(
    entities = [CollectionEntity::class, ItemEntity::class, TermEntity::class, SegmentCacheEntity::class],
    version = 5,
    exportSchema = false,
)
abstract class OffreadDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao

    abstract fun itemDao(): ItemDao

    abstract fun termDao(): TermDao

    abstract fun segmentCacheDao(): SegmentCacheDao
}
