package com.android.offread.importer.domain.usecase

import com.android.offread.importer.domain.WebNovelImporter
import com.android.offread.importer.domain.model.WebNovelMetadata
import javax.inject.Inject

/**
 * F-012: 입력 URL 을 검증(비어있음/미지원 사이트)하고 작품 메타를 인식한다.
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

/** 지원하지 않는 사이트일 때(P-02 화이트리스트 밖). */
object UnsupportedSiteException : IllegalArgumentException("아직 지원하지 않는 사이트예요.")
