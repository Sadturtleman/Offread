package com.android.offread.translate.data.di

import com.android.offread.translate.data.DataStoreTranslationEnginePreference
import com.android.offread.translate.data.LlmEngine
import com.android.offread.translate.data.LlmTranslationEngine
import com.android.offread.translate.data.MlKitEngine
import com.android.offread.translate.data.MlKitTranslationEngine
import com.android.offread.translate.data.RoomSegmentCache
import com.android.offread.translate.data.SwitchingTranslationEngine
import com.android.offread.translate.data.TermGlossaryProvider
import com.android.offread.translate.data.TermRepositorySuggestionSink
import com.android.offread.translate.data.TranslationModelRepositoryImpl
import com.android.offread.translate.data.WorkManagerPretranslateScheduler
import com.android.offread.translate.domain.GlossaryProvider
import com.android.offread.translate.domain.PretranslateScheduler
import com.android.offread.translate.domain.SegmentCache
import com.android.offread.translate.domain.TermSuggestionSink
import com.android.offread.translate.domain.TranslationEngine
import com.android.offread.translate.domain.TranslationEnginePreference
import com.android.offread.translate.domain.TranslationModelRepository
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

    /** 실제로 쓰이는 엔진은 설정 선택에 따라 갈린다(F-020). */
    @Binds
    @Singleton
    abstract fun bindTranslationEngine(impl: SwitchingTranslationEngine): TranslationEngine

    @Binds
    @Singleton
    @MlKitEngine
    abstract fun bindMlKitEngine(impl: MlKitTranslationEngine): TranslationEngine

    @Binds
    @Singleton
    @LlmEngine
    abstract fun bindLlmEngine(impl: LlmTranslationEngine): TranslationEngine

    @Binds
    @Singleton
    abstract fun bindTranslationEnginePreference(impl: DataStoreTranslationEnginePreference): TranslationEnginePreference

    @Binds
    @Singleton
    abstract fun bindGlossaryProvider(impl: TermGlossaryProvider): GlossaryProvider

    @Binds
    @Singleton
    abstract fun bindTermSuggestionSink(impl: TermRepositorySuggestionSink): TermSuggestionSink

    @Binds
    @Singleton
    abstract fun bindPretranslateScheduler(impl: WorkManagerPretranslateScheduler): PretranslateScheduler

    @Binds
    @Singleton
    abstract fun bindTranslationModelRepository(impl: TranslationModelRepositoryImpl): TranslationModelRepository
}
