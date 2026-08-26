package com.android.offread.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * 앱 로컬 DB(온디바이스). MVP 는 번역 세그먼트 캐시 하나만 담는다.
 */
@Database(entities = [SegmentCacheEntity::class], version = 6, exportSchema = false)
abstract class OffreadDatabase : RoomDatabase() {
    abstract fun segmentCacheDao(): SegmentCacheDao
}
