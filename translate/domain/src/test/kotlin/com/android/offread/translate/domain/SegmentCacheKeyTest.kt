package com.android.offread.translate.domain

import com.android.offread.translate.domain.model.SegmentCacheKey
import com.android.offread.translate.domain.model.sha256
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SegmentCacheKeyTest {
    private val original = "ルーデウスは分かれ道の前で立ち止まった。"

    @Test
    fun `같은 원문과 모델이면 같은 키다`() {
        assertEquals(SegmentCacheKey.of(original, "v1"), SegmentCacheKey.of(original, "v1"))
    }

    @Test
    fun `모델이 바뀌면 옛 번역을 재사용하지 않는다`() {
        assertNotEquals(SegmentCacheKey.of(original, "v1"), SegmentCacheKey.of(original, "v2"))
    }

    @Test
    fun `원문이 한 글자만 달라도 키가 다르다`() {
        assertNotEquals(
            SegmentCacheKey.of(original, "v1").contentHash,
            SegmentCacheKey.of(original + "。", "v1").contentHash,
        )
    }

    @Test
    fun `해시는 SHA-256 hex 64 자다`() {
        val hash = original.sha256()

        assertEquals(64, hash.length)
        assertEquals(hash, hash.lowercase())
    }
}
