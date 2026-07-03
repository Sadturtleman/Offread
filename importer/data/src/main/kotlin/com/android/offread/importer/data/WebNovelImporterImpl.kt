package com.android.offread.importer.data

import com.android.offread.core.entity.SerialStatus
import com.android.offread.importer.domain.WebNovelImporter
import com.android.offread.importer.domain.model.WebNovelMetadata
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * [WebNovelImporter] 스텁 어댑터(F-012). 지원 사이트 화이트리스트(P-02)만 실제로 판정하고,
 * 메타 추출은 캔드 값을 돌려준다. 실제 HTTP 수집·파싱(수집 매너 P-02)은 후속 인프라 태스크에서 교체한다.
 */
class WebNovelImporterImpl
    @Inject
    constructor() : WebNovelImporter {
        override fun isSupported(url: String): Boolean = SUPPORTED_HOSTS.any { url.contains(it, ignoreCase = true) }

        override suspend fun recognize(url: String): WebNovelMetadata {
            delay(STUB_LATENCY_MILLIS)
            val siteName = if (url.contains("kakuyomu", ignoreCase = true)) "카쿠요무" else "소설가가 되자"
            return WebNovelMetadata(
                siteName = siteName,
                title = "무직전생 ~이세계에 갔으면 최선을 다한다~",
                author = "理不尽な孫の手",
                totalChapters = 286,
                serialStatus = SerialStatus.ONGOING,
                sourceUrl = url,
            )
        }

        private companion object {
            // P-02: ToS·robots 검토를 통과한 사이트만. MVP 초기 목록.
            val SUPPORTED_HOSTS = setOf("ncode.syosetu.com", "syosetu.com", "kakuyomu.jp")
            const val STUB_LATENCY_MILLIS = 500L
        }
    }
