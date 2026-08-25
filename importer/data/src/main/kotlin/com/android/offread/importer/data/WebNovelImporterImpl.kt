package com.android.offread.importer.data

import com.android.offread.core.entity.SerialStatus
import com.android.offread.importer.domain.WebNovelImporter
import com.android.offread.importer.domain.model.WebNovelMetadata
import kotlinx.coroutines.delay
import java.net.URI
import javax.inject.Inject

/**
 * [WebNovelImporter] 스텁 어댑터(F-012).
 *
 * URL 이 http/https 형태이기만 하면 받아들인다 — 지원 사이트 화이트리스트는 해제된 상태다.
 *
 * NOTE(P-02): 스펙은 ToS·robots 검토를 통과한 사이트만 받는 화이트리스트 방식을 명시한다.
 * 실제 HTTP 수집(#33)을 붙일 때 수집 매너(사이트당 1연결·요청 간격 1초·식별 가능한 User-Agent)와
 * 함께 이 정책을 다시 정해야 한다.
 *
 * 메타 추출은 아직 캔드 값이고, 사이트명만 URL 호스트에서 만든다.
 */
class WebNovelImporterImpl
    @Inject
    constructor() : WebNovelImporter {
        override fun isSupported(url: String): Boolean = hostOf(url) != null

        override suspend fun recognize(url: String): WebNovelMetadata {
            delay(STUB_LATENCY_MILLIS)
            return WebNovelMetadata(
                siteName = siteNameOf(url),
                title = "무직전생 ~이세계에 갔으면 최선을 다한다~",
                author = "理不尽な孫の手",
                totalChapters = 286,
                serialStatus = SerialStatus.ONGOING,
                sourceUrl = url,
            )
        }

        /** http/https 이고 호스트가 있는 주소만 다룬다. */
        private fun hostOf(url: String): String? {
            val uri = runCatching { URI(url.trim()) }.getOrNull() ?: return null
            val scheme = uri.scheme?.lowercase()
            if (scheme != "http" && scheme != "https") return null
            return uri.host?.takeIf { it.isNotBlank() }
        }

        /** 알려진 사이트는 한국어 표기로, 그 외는 호스트를 그대로 보여준다. */
        private fun siteNameOf(url: String): String {
            val host = hostOf(url)?.removePrefix("www.") ?: return "알 수 없는 사이트"
            return KNOWN_SITE_NAMES.entries.firstOrNull { host.endsWith(it.key) }?.value ?: host
        }

        private companion object {
            val KNOWN_SITE_NAMES =
                mapOf(
                    "syosetu.com" to "소설가가 되자",
                    "kakuyomu.jp" to "카쿠요무",
                )
            const val STUB_LATENCY_MILLIS = 500L
        }
    }
