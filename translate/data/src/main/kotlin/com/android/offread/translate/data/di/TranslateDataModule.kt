package com.android.offread.translate.data.di

import com.android.offread.translate.data.DataStoreTranslationEnginePreference
import com.android.offread.translate.data.FileLlmModelStore
import com.android.offread.translate.data.JsoupWebPageSource
import com.android.offread.translate.data.LiteRtLmTranslationEngine
import com.android.offread.translate.data.MlKitEngine
import com.android.offread.translate.data.MlKitTranslationEngine
import com.android.offread.translate.data.RoomSegmentCache
import com.android.offread.translate.data.SwitchingTranslationEngine
import com.android.offread.translate.data.TranslateGemmaEngine
import com.android.offread.translate.domain.LlmModelStore
import com.android.offread.translate.domain.SegmentCache
import com.android.offread.translate.domain.TranslationEngine
import com.android.offread.translate.domain.TranslationEnginePreference
import com.android.offread.translate.domain.WebPageSource
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

    @Binds
    @Singleton
    abstract fun bindWebPageSource(impl: JsoupWebPageSource): WebPageSource

    /** 실제로 쓰이는 엔진은 설정 선택에 따라 갈린다. */
    @Binds
    @Singleton
    abstract fun bindTranslationEngine(impl: SwitchingTranslationEngine): TranslationEngine

    @Binds
    @Singleton
    @MlKitEngine
    abstract fun bindMlKitEngine(impl: MlKitTranslationEngine): TranslationEngine

    @Binds
    @Singleton
    @TranslateGemmaEngine
    abstract fun bindTranslateGemmaEngine(impl: LiteRtLmTranslationEngine): TranslationEngine

    @Binds
    @Singleton
    abstract fun bindLlmModelStore(impl: FileLlmModelStore): LlmModelStore

    @Binds
    @Singleton
    abstract fun bindTranslationEnginePreference(impl: DataStoreTranslationEnginePreference): TranslationEnginePreference
}
