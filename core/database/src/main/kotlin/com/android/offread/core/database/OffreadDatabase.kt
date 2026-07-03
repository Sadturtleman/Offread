package com.android.offread.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * 앱 로컬 DB(P-01 온디바이스). 라이브러리·컬렉션·아이템·용어맵·캐시 메타를 담는다.
 * 지금은 컬렉션 테이블만. 아이템/용어/캐시 테이블은 관련 기능(가져오기·리더·용어맵)에서 추가한다.
 */
@Database(
    entities = [CollectionEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class OffreadDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
}
