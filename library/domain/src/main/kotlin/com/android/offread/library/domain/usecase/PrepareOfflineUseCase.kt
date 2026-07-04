package com.android.offread.library.domain.usecase

import com.android.offread.core.entity.TranslationStatus
import com.android.offread.library.domain.LibraryRepository
import javax.inject.Inject

/**
 * F-008 오프라인 준비: 다음 N화를 선번역·선캐시(F-022)한다.
 *
 * NOTE: 실제 백그라운드 선번역 큐(WorkManager, 배터리·발열 보호)는 후속 인프라 태스크.
 * 현재는 아이템 번역 상태를 CACHED 로 전이시키는 스텁이다.
 */
class PrepareOfflineUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
    ) {
        suspend operator fun invoke(itemId: String) {
            libraryRepository.updateItemTranslationStatus(itemId, TranslationStatus.CACHED)
        }
    }
