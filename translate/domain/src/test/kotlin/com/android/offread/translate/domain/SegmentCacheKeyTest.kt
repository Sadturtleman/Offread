package com.android.offread.translate.domain

import com.android.offread.translate.domain.model.SegmentCacheKey
import com.android.offread.translate.domain.model.sha256
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SegmentCacheKeyTest {
    private val original = "ルーデウスは分かれ道の前で立ち止まった。"

    @Test
    fun `같은 원문 컬렉션 모델이면 같은 키다`() {
        assertEquals(
            SegmentCacheKey.of(original, "c1", "v1"),
            SegmentCacheKey.of(original, "c1", "v1"),
        )
    }

    @Test
    fun `컬렉션이 다르면 키가 달라 교차 오염이 없다`() {
        assertNotEquals(
            SegmentCacheKey.of(original, "c1", "v1"),
            SegmentCacheKey.of(original, "c2", "v1"),
        )
    }

    @Test
    fun `모델 버전이 바뀌면 옛 번역을 재사용하지 않는다`() {
        assertNotEquals(
            SegmentCacheKey.of(original, "c1", "v1"),
            SegmentCacheKey.of(original, "c1", "v2"),
        )
    }

    @Test
    fun `원문이 한 글자만 달라도 키가 다르다`() {
        assertNotEquals(
            SegmentCacheKey.of(original, "c1", "v1").contentHash,
            SegmentCacheKey.of(original + "。", "c1", "v1").contentHash,
        )
    }

    @Test
    fun `해시는 SHA-256 hex 64 자다`() {
        val hash = original.sha256()

        assertEquals(64, hash.length)
        assertEquals(hash, hash.lowercase())
    }
}
