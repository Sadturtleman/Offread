package com.android.offread.reader.data.di

import com.android.offread.reader.data.ChapterContentRepositoryImpl
import com.android.offread.reader.domain.ChapterContentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReaderDataModule {
    @Binds
    @Singleton
    abstract fun bindChapterContentRepository(impl: ChapterContentRepositoryImpl): ChapterContentRepository
}
