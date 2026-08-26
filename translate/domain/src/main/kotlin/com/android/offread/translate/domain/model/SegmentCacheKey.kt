package com.android.offread.translate.domain.model

import java.security.MessageDigest

/**
 * 세그먼트 캐시 키 = 콘텐츠 해시 + 모델 버전.
 *
 * 모델(엔진)이 바뀌면 자연히 다른 키가 되어 옛 번역이 재사용되지 않는다.
 * 원문은 저장하지 않고 해시만 남긴다.
 */
data class SegmentCacheKey(
    val contentHash: String,
    val modelVersion: String,
) {
    companion object {
        fun of(
            original: String,
            modelVersion: String,
        ): SegmentCacheKey = SegmentCacheKey(contentHash = original.sha256(), modelVersion = modelVersion)
    }
}

/** 콘텐츠 해시(SHA-256, 소문자 hex). */
fun String.sha256(): String =
    MessageDigest
        .getInstance("SHA-256")
        .digest(toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
