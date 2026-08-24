package com.android.offread.settings.domain.model

import com.android.offread.core.entity.TranslationModel
import com.android.offread.translate.domain.model.ModelDownloadStatus

/**
 * S-02 모델 관리 목록의 한 줄(F-029).
 *
 * @property installed 설치 완료 — 삭제로 용량을 되돌릴 수 있다
 * @property status 진행 중이면 다운로드 상태, 아니면 null
 */
data class ManagedModel(
    val model: TranslationModel,
    val installed: Boolean,
    val status: ModelDownloadStatus?,
) {
    val isBusy: Boolean
        get() =
            status is ModelDownloadStatus.Queued ||
                status is ModelDownloadStatus.Downloading ||
                status is ModelDownloadStatus.Paused
}
