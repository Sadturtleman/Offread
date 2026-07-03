package com.android.offread.onboarding.presentation.download

import com.android.offread.onboarding.domain.model.ModelDownloadStatus

/** 진행 카드 하단 보조 문구: "42% · 남은 시간 4분 · 12.4MB/s" 형태. */
internal fun ModelDownloadStatus.progressLine(): String =
    when (this) {
        ModelDownloadStatus.Queued -> "대기 중"
        ModelDownloadStatus.Paused -> "일시정지됨"
        ModelDownloadStatus.Completed -> "완료"
        is ModelDownloadStatus.Failed -> "실패 · 재시도 ${attempt}회"
        is ModelDownloadStatus.Downloading -> {
            val percent = (fraction * 100).toInt()
            val eta = remainingSeconds?.let { " · 남은 시간 ${formatDuration(it)}" }.orEmpty()
            val speed = if (bytesPerSecond > 0) " · ${formatSpeed(bytesPerSecond)}" else ""
            "$percent%$eta$speed"
        }
    }

/** 진행바 값(0.0~1.0). 완료는 1, 그 외 미측정은 0. */
internal fun ModelDownloadStatus.fractionOrZero(): Float =
    when (this) {
        is ModelDownloadStatus.Downloading -> fraction
        ModelDownloadStatus.Completed -> 1f
        else -> 0f
    }

private fun formatDuration(seconds: Long): String {
    if (seconds < 60) return "${seconds}초"
    val minutes = seconds / 60
    return "${minutes}분"
}

private fun formatSpeed(bytesPerSecond: Long): String {
    val mbps = bytesPerSecond.toDouble() / (1024 * 1024)
    if (mbps >= 1.0) return "%.1fMB/s".format(mbps)
    val kbps = bytesPerSecond.toDouble() / 1024
    return "%.0fKB/s".format(kbps)
}
