package com.android.offread.translate.data

import com.android.offread.translate.domain.WebPageSource
import com.android.offread.translate.domain.model.WebPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject

/**
 * Jsoup 웹페이지 수집 어댑터.
 *
 * 수집 매너: 앱을 식별할 수 있는 User-Agent 를 보내고 타임아웃을 건다. 사용자가 직접 넣은
 * 주소만 한 번씩 받아오며, 링크를 따라다니는 크롤링은 하지 않는다.
 */
class JsoupWebPageSource
    @Inject
    constructor() : WebPageSource {
        override suspend fun fetch(url: String): WebPage =
            withContext(Dispatchers.IO) {
                val document =
                    runCatching {
                        Jsoup
                            .connect(url.trim())
                            .userAgent(USER_AGENT)
                            .timeout(TIMEOUT_MILLIS)
                            .followRedirects(true)
                            .get()
                    }.getOrElse { throw PageFetchException(it) }

                val text = HtmlContentExtractor.extract(url, document)
                if (text.isBlank()) throw EmptyPageException
                WebPage(url = url, title = document.title().ifBlank { url }, text = text)
            }

        private companion object {
            const val USER_AGENT = "Offread/0.1 (on-device translation reader)"
            const val TIMEOUT_MILLIS = 15_000
        }
    }

/** 페이지를 받아오지 못했을 때(네트워크·차단·주소 오류). */
class PageFetchException(
    cause: Throwable,
) : IllegalStateException("페이지를 가져오지 못했어요. 주소와 네트워크를 확인해 주세요.", cause)

/** 본문 텍스트를 찾지 못했을 때. */
object EmptyPageException : IllegalStateException("이 페이지에서 번역할 본문을 찾지 못했어요.")
