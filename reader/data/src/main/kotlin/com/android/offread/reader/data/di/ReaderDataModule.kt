package com.android.offread.reader.data.di

import com.android.offread.reader.data.ChapterContentRepositoryImpl
import com.android.offread.reader.data.StubChapterTextSource
import com.android.offread.reader.domain.ChapterContentRepository
import com.android.offread.translate.domain.ChapterTextSource
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

    /** 리더와 선번역 큐(F-022)가 같은 원문을 쓰도록 원문 포트도 여기서 바인딩한다. */
    @Binds
    @Singleton
    abstract fun bindChapterTextSource(impl: StubChapterTextSource): ChapterTextSource
}
