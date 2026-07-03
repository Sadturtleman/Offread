package com.android.offread.core.ui.helper

import androidx.compose.runtime.compositionLocalOf
import com.android.offread.core.domain.helper.MessageHelper

val LocalMessageHelper = compositionLocalOf<MessageHelper> { error("No MessageHelper provided!") }
