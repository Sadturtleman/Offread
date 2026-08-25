package com.android.offread.importer.domain.usecase

import com.android.offread.importer.domain.WebNovelImporter
import com.android.offread.importer.domain.model.WebNovelMetadata
import javax.inject.Inject

/**
 * F-012: 입력 URL 을 검증(비어있음/어댑터가 다룰 수 없는 주소)하고 작품 메타를 인식한다.
 */
class RecognizeWebNovelUseCase
    @Inject
    constructor(
        private val webNovelImporter: WebNovelImporter,
    ) {
        suspend operator fun invoke(url: String): Result<WebNovelMetadata> {
            val trimmed = url.trim()
            if (trimmed.isEmpty()) return Result.failure(EmptyUrlException)
            if (!webNovelImporter.isSupported(trimmed)) return Result.failure(UnsupportedSiteException)
            return runCatching { webNovelImporter.recognize(trimmed) }
        }
    }

/** URL 이 비어 있을 때. */
object EmptyUrlException : IllegalArgumentException("작품 URL 을 입력해 주세요.")

/** 어댑터가 다룰 수 없는 주소일 때(형식 오류 또는 미지원 사이트). */
object UnsupportedSiteException : IllegalArgumentException("가져올 수 없는 주소예요. http 또는 https 주소인지 확인해 주세요.")
