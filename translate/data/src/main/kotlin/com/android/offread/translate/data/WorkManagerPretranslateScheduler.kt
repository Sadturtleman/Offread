package com.android.offread.translate.data

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.android.offread.translate.domain.PretranslateRequest
import com.android.offread.translate.domain.PretranslateScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [PretranslateScheduler] 의 WorkManager 어댑터(F-022).
 *
 * 아이템마다 고유 작업으로 넣고 [ExistingWorkPolicy.REPLACE] 를 써서, 같은 작품을 다시
 * 준비하면 옛 예약이 쌓이지 않게 한다. 배터리가 부족하면 시작하지 않는다.
 */
@Singleton
class WorkManagerPretranslateScheduler
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : PretranslateScheduler {
        override fun schedule(request: PretranslateRequest) {
            if (request.count <= 0) return
            val work =
                OneTimeWorkRequestBuilder<PretranslateWorker>()
                    .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                    .setBackoffCriteria(BackoffPolicy.LINEAR, BACKOFF_MINUTES, TimeUnit.MINUTES)
                    .setInputData(
                        workDataOf(
                            PretranslateWorker.KEY_ITEM_ID to request.itemId,
                            PretranslateWorker.KEY_COLLECTION_ID to request.collectionId,
                            PretranslateWorker.KEY_FROM_CHAPTER to request.fromChapter,
                            PretranslateWorker.KEY_COUNT to request.count,
                            PretranslateWorker.KEY_LANGUAGE_PAIR to request.languagePair.name,
                        ),
                    ).build()
            WorkManager.getInstance(context).enqueueUniqueWork(workName(request.itemId), ExistingWorkPolicy.REPLACE, work)
        }

        override fun cancel(itemId: String) {
            WorkManager.getInstance(context).cancelUniqueWork(workName(itemId))
        }

        private fun workName(itemId: String) = "$WORK_PREFIX$itemId"

        private companion object {
            const val WORK_PREFIX = "pretranslate-"
            const val BACKOFF_MINUTES = 10L
        }
    }
