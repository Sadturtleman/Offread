package com.android.offread.library.data.di

import com.android.offread.library.data.ImporterChapterSource
import com.android.offread.library.data.LibraryRepositoryImpl
import com.android.offread.library.data.NoopTranslationCache
import com.android.offread.library.domain.ChapterSource
import com.android.offread.library.domain.LibraryRepository
import com.android.offread.library.domain.TranslationCache
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LibraryDataModule {
    @Binds
    @Singleton
    abstract fun bindLibraryRepository(impl: LibraryRepositoryImpl): LibraryRepository

    @Binds
    @Singleton
    abstract fun bindChapterSource(impl: ImporterChapterSource): ChapterSource

    @Binds
    @Singleton
    abstract fun bindTranslationCache(impl: NoopTranslationCache): TranslationCache
}
