package com.android.offread.translate.data

import javax.inject.Qualifier

/** ML Kit 어댑터. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MlKitEngine

/** TranslateGemma(LiteRT-LM) 어댑터. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TranslateGemmaEngine
