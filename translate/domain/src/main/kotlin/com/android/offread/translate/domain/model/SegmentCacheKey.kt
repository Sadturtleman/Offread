package com.android.offread.translate.domain.model

import java.security.MessageDigest

/**
 * F-021 세그먼트 캐시 키 = 콘텐츠 해시 + collectionId + 모델 버전(§5).
 *
 * - collectionId 를 키에 넣어 **컬렉션 간 교차 오염을 막는다**. 같은 원문이라도 컬렉션마다
 *   용어맵이 다르므로 번역 결과가 달라야 한다.
 * - 모델 버전이 바뀌면 자연히 다른 키가 되어 옛 번역이 재사용되지 않는다.
 */
data class SegmentCacheKey(
    val contentHash: String,
    val collectionId: String,
    val modelVersion: String,
) {
    companion object {
        /** 원문에서 키를 만든다. 원문은 저장하지 않고 해시만 남긴다. */
        fun of(
            original: String,
            collectionId: String,
            modelVersion: String,
        ): SegmentCacheKey =
            SegmentCacheKey(
                contentHash = original.sha256(),
                collectionId = collectionId,
                modelVersion = modelVersion,
            )
    }
}

/** 콘텐츠 해시(SHA-256, 소문자 hex). */
fun String.sha256(): String =
    MessageDigest
        .getInstance("SHA-256")
        .digest(toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
