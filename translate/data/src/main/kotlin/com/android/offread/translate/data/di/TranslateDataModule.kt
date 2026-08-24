package com.android.offread.translate.data.di

import com.android.offread.translate.data.RoomSegmentCache
import com.android.offread.translate.domain.SegmentCache
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TranslateDataModule {
    @Binds
    @Singleton
    abstract fun bindSegmentCache(impl: RoomSegmentCache): SegmentCache
}
