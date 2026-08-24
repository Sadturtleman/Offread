package com.android.offread.library.domain.usecase

import com.android.offread.core.entity.LanguagePair
import com.android.offread.core.entity.TranslationStatus
import com.android.offread.library.domain.LibraryRepository
import com.android.offread.translate.domain.PretranslateRequest
import com.android.offread.translate.domain.PretranslateScheduler
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * F-008 오프라인 준비: 다음 N화를 선번역 큐에 넣는다(F-022). 기본 N=5.
 *
 * 읽던 다음 화부터 예약하고, 남은 화가 그보다 적으면 남은 만큼만 넣는다.
 * 큐가 실제로 도는 동안 배지는 '번역 중'으로 두고, 완료 전이는 캐시 상태에서 나온다.
 */
class PrepareOfflineUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
        private val pretranslateScheduler: PretranslateScheduler,
    ) {
        suspend operator fun invoke(
            itemId: String,
            count: Int = DEFAULT_COUNT,
        ): Int {
            val item = libraryRepository.observeItem(itemId).first() ?: return 0
            val fromChapter = (item.lastReadChapter + 1).coerceAtLeast(1)
            val remaining = (item.totalChapters - fromChapter + 1).coerceAtLeast(0)
            val scheduled = minOf(count, remaining)
            if (scheduled <= 0) return 0

            libraryRepository.updateItemTranslationStatus(itemId, TranslationStatus.TRANSLATING)
            pretranslateScheduler.schedule(
                PretranslateRequest(
                    itemId = itemId,
                    collectionId = item.collectionId,
                    fromChapter = fromChapter,
                    count = scheduled,
                    languagePair = DEFAULT_PAIR,
                ),
            )
            return scheduled
        }

        private companion object {
            const val DEFAULT_COUNT = 5

            /**
             * 원문 언어는 수집 단계에서 확정된다(F-012 인프라). 그 전까지 MVP 주 대상인 일→한 고정.
             */
            val DEFAULT_PAIR = LanguagePair.JA_KO
        }
    }
