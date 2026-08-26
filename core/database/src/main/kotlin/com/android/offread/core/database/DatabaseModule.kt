package com.android.offread.core.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private const val DB_NAME = "offread.db"

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): OffreadDatabase =
        Room
            .databaseBuilder(context, OffreadDatabase::class.java, DB_NAME)
            // 캐시뿐이라 스키마가 바뀌면 다시 만들어도 잃을 게 없다(다시 번역하면 된다).
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideSegmentCacheDao(database: OffreadDatabase): SegmentCacheDao = database.segmentCacheDao()
}
