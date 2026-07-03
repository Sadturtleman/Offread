package com.android.offread.core.ui.helper

import com.android.offread.core.domain.helper.MessageHelper
import com.android.offread.core.domain.helper.NavigationHelper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UiBindingsModule {
    @Binds
    @Singleton
    abstract fun bindNavigationHelper(impl: NavigationHelperImpl): NavigationHelper

    @Binds
    @Singleton
    abstract fun bindMessageHelper(impl: MessageHelperImpl): MessageHelper
}
