package com.android.offread.importer.data.di

import com.android.offread.importer.data.WebNovelImporterImpl
import com.android.offread.importer.domain.WebNovelImporter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ImporterDataModule {
    @Binds
    @Singleton
    abstract fun bindWebNovelImporter(impl: WebNovelImporterImpl): WebNovelImporter
}
