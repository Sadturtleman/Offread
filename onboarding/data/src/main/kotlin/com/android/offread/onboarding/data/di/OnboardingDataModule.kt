package com.android.offread.onboarding.data.di

import com.android.offread.onboarding.data.OnboardingRepositoryImpl
import com.android.offread.onboarding.data.TranslationModelRepositoryImpl
import com.android.offread.onboarding.domain.OnboardingRepository
import com.android.offread.onboarding.domain.TranslationModelRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OnboardingDataModule {
    @Binds
    @Singleton
    abstract fun bindOnboardingRepository(impl: OnboardingRepositoryImpl): OnboardingRepository

    @Binds
    @Singleton
    abstract fun bindTranslationModelRepository(impl: TranslationModelRepositoryImpl): TranslationModelRepository
}
