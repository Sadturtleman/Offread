package com.android.offread.library.domain.usecase

import com.android.offread.core.entity.ItemType
import com.android.offread.core.entity.SerialStatus
import com.android.offread.core.entity.TranslationStatus
import com.android.offread.library.domain.LibraryRepository
import com.android.offread.library.domain.model.LibraryItem
import javax.inject.Inject

/**
 * F-012: 인식된 웹소설 메타데이터를 지정한 컬렉션에 아이템으로 저장한다.
 * 최초 저장 시 번역 상태는 미번역([TranslationStatus.UNTRANSLATED]).
 */
class AddWebNovelUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
    ) {
        suspend operator fun invoke(request: Request): Result<String> {
            if (request.collectionId.isBlank()) {
                return Result.failure(NoTargetCollectionException)
            }
            val item =
                LibraryItem(
                    id = "",
                    collectionId = request.collectionId,
                    type = ItemType.WEBNOVEL,
                    title = request.title,
                    author = request.author,
                    sourceUrl = request.sourceUrl,
                    siteName = request.siteName,
                    totalChapters = request.totalChapters,
                    serialStatus = request.serialStatus,
                    translationStatus = TranslationStatus.UNTRANSLATED,
                    updatedAt = 0,
                )
            return Result.success(libraryRepository.addItem(item))
        }

        data class Request(
            val collectionId: String,
            val title: String,
            val author: String,
            val sourceUrl: String,
            val siteName: String,
            val totalChapters: Int,
            val serialStatus: SerialStatus,
        )
    }

/** 저장할 컬렉션을 지정하지 않았을 때. */
object NoTargetCollectionException : IllegalArgumentException("저장할 컬렉션을 선택해 주세요.")
