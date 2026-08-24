package com.android.offread.library.data

import com.android.offread.library.domain.TranslationCache
import javax.inject.Inject

/**
 * [TranslationCache] no-op 어댑터. 실제 세그먼트 캐시(F-020/F-021, collectionId 포함 키)가
 * 생기면 해당 인프라 어댑터로 바인딩을 교체한다.
 */
class NoopTranslationCache
    @Inject
    constructor() : TranslationCache {
        override suspend fun invalidateItem(itemId: String) = Unit
    }
