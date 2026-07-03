package com.android.offread.library.domain.usecase

import com.android.offread.library.domain.LibraryRepository
import javax.inject.Inject

/** F-006: 컬렉션 생성. 이름은 공백일 수 없다. 생성된 id 반환. */
class CreateCollectionUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
    ) {
        suspend operator fun invoke(
            name: String,
            parentId: String? = null,
        ): Result<String> {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) return Result.failure(BlankCollectionNameException)
            return Result.success(libraryRepository.createCollection(trimmed, parentId))
        }
    }

/** F-006: 컬렉션 이름 변경. 이름은 공백일 수 없다. */
class RenameCollectionUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
    ) {
        suspend operator fun invoke(
            id: String,
            name: String,
        ): Result<Unit> {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) return Result.failure(BlankCollectionNameException)
            libraryRepository.renameCollection(id, trimmed)
            return Result.success(Unit)
        }
    }

/** F-006: 컬렉션 삭제(즉시, 연쇄). */
class DeleteCollectionUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
    ) {
        suspend operator fun invoke(id: String) = libraryRepository.deleteCollection(id)
    }

/** 컬렉션 이름이 공백일 때. */
object BlankCollectionNameException : IllegalArgumentException("컬렉션 이름을 입력해 주세요.")
