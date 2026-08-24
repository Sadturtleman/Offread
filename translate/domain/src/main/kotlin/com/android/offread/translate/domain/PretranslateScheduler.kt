package com.android.offread.translate.domain

import com.android.offread.core.entity.LanguagePair

/**
 * F-022 선번역 큐 예약 포트. 실제 큐(WorkManager)·배터리/발열 보호는 어댑터에 감춘다.
 */
interface PretranslateScheduler {
    fun schedule(request: PretranslateRequest)

    /** 아이템의 예약분을 취소한다(작품 삭제·이동 등). */
    fun cancel(itemId: String)
}

/**
 * @property fromChapter 선번역을 시작할 화(1-based)
 * @property count 몇 화를 미리 번역할지(F-008 오프라인 준비 기본 5)
 */
data class PretranslateRequest(
    val itemId: String,
    val collectionId: String,
    val fromChapter: Int,
    val count: Int,
    val languagePair: LanguagePair,
)
