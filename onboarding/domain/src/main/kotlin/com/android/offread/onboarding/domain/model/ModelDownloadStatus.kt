package com.android.offread.onboarding.domain.model

/**
 * 번역 모델 1개의 다운로드 상태(F-003).
 */
sealed interface ModelDownloadStatus {
    /** 대기열에 있으나 아직 시작 전. */
    data object Queued : ModelDownloadStatus

    /**
     * 내려받는 중.
     * @property downloadedBytes 지금까지 받은 바이트
     * @property totalBytes 전체 바이트
     * @property bytesPerSecond 순간 속도(0 이면 미측정)
     */
    data class Downloading(
        val downloadedBytes: Long,
        val totalBytes: Long,
        val bytesPerSecond: Long,
    ) : ModelDownloadStatus {
        /** 0.0~1.0. totalBytes 가 0 이면 0. */
        val fraction: Float get() = if (totalBytes <= 0) 0f else (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f)

        /** 남은 시간(초). 속도가 0 이면 null. */
        val remainingSeconds: Long? get() = if (bytesPerSecond <= 0) null else (totalBytes - downloadedBytes) / bytesPerSecond
    }

    /** 사용자가 일시정지. */
    data object Paused : ModelDownloadStatus

    /** 다운로드·무결성 검증 완료(설치됨). */
    data object Completed : ModelDownloadStatus

    /**
     * 실패. [attempt] 회 재시도했다.
     * 자동 재시도 3회(F-003) 소진 후에도 실패하면 이 상태로 노출한다.
     */
    data class Failed(
        val attempt: Int,
    ) : ModelDownloadStatus
}
