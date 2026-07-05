package com.android.offread.terms.data.di

import com.android.offread.terms.data.TermRepositoryImpl
import com.android.offread.terms.domain.TermRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TermsDataModule {
    @Binds
    @Singleton
    abstract fun bindTermRepository(impl: TermRepositoryImpl): TermRepository
}
