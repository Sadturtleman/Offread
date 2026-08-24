package com.android.offread.translate.data

import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.android.offread.core.entity.LanguagePair
import com.android.offread.translate.domain.usecase.PretranslateChapterUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * F-022 선번역 워커. 다음 화들을 미리 번역해 캐시를 채운다.
 *
 * 배터리는 WorkManager 제약(requiresBatteryNotLow)으로 지키고, 발열은 매 화 시작 전에
 * 기기 상태를 확인해 임계치를 넘으면 [Result.retry] 로 물러난다 — WorkManager 가 나중에
 * 다시 부르므로 '중단 후 재개'가 된다.
 */
@HiltWorker
class PretranslateWorker
    @AssistedInject
    constructor(
        @Assisted appContext: Context,
        @Assisted params: WorkerParameters,
        private val pretranslateChapter: PretranslateChapterUseCase,
    ) : CoroutineWorker(appContext, params) {
        override suspend fun doWork(): Result {
            val itemId = inputData.getString(KEY_ITEM_ID) ?: return Result.failure()
            val collectionId = inputData.getString(KEY_COLLECTION_ID) ?: return Result.failure()
            val pair =
                inputData.getString(KEY_LANGUAGE_PAIR)?.let { runCatching { LanguagePair.valueOf(it) }.getOrNull() }
                    ?: return Result.failure()
            val fromChapter = inputData.getInt(KEY_FROM_CHAPTER, 1)
            val count = inputData.getInt(KEY_COUNT, 0)

            for (offset in 0 until count) {
                if (isStopped) return Result.success()
                if (isOverheating()) return Result.retry()
                val chapterIndex = fromChapter + offset
                runCatching { pretranslateChapter(itemId, collectionId, chapterIndex, pair) }
                    .onFailure { return Result.retry() }
            }
            return Result.success()
        }

        /**
         * 발열 임계: MODERATE 이상이면 물러난다. 기기가 이미 스로틀링을 시작한 시점이라
         * 사용자가 보고 있는 화면의 반응성을 지키려면 백그라운드 추론을 멈추는 편이 낫다.
         * (스펙의 '결정 필요' 항목 — 실기기 측정 뒤 조정한다.)
         */
        private fun isOverheating(): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
            val powerManager = applicationContext.getSystemService(PowerManager::class.java) ?: return false
            return powerManager.currentThermalStatus >= PowerManager.THERMAL_STATUS_MODERATE
        }

        companion object {
            const val KEY_ITEM_ID = "itemId"
            const val KEY_COLLECTION_ID = "collectionId"
            const val KEY_FROM_CHAPTER = "fromChapter"
            const val KEY_COUNT = "count"
            const val KEY_LANGUAGE_PAIR = "languagePair"
        }
    }
