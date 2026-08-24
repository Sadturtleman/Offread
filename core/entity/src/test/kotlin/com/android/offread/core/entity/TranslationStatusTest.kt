package com.android.offread.core.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslationStatusTest {
    @Test
    fun `하나도 번역되지 않았으면 미번역이다`() {
        assertEquals(TranslationStatus.UNTRANSLATED, TranslationStatus.of(total = 10, translated = 0))
    }

    @Test
    fun `일부만 번역되었으면 번역 중이다`() {
        assertEquals(TranslationStatus.TRANSLATING, TranslationStatus.of(total = 10, translated = 3))
    }

    @Test
    fun `전부 번역되었으면 캐시됨이다`() {
        assertEquals(TranslationStatus.CACHED, TranslationStatus.of(total = 10, translated = 10))
    }

    @Test
    fun `대상이 없으면 미번역으로 본다`() {
        assertEquals(TranslationStatus.UNTRANSLATED, TranslationStatus.of(total = 0, translated = 0))
    }

    @Test
    fun `클라우드 폴백이 진행도보다 우선한다`() {
        assertEquals(
            TranslationStatus.CLOUD_FALLBACK,
            TranslationStatus.of(total = 10, translated = 10, cloudFallback = true),
        )
    }

    @Test
    fun `오프라인 열람은 캐시됨에서만 보장된다`() {
        assertTrue(TranslationStatus.CACHED.isOfflineReady)
        assertFalse(TranslationStatus.CLOUD_FALLBACK.isOfflineReady)
        assertFalse(TranslationStatus.TRANSLATING.isOfflineReady)
        assertFalse(TranslationStatus.UNTRANSLATED.isOfflineReady)
    }
}
