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
            .build()

    @Provides
    fun provideCollectionDao(database: OffreadDatabase): CollectionDao = database.collectionDao()
}
