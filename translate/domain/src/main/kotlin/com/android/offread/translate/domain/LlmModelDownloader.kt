package com.android.offread.translate.domain

import kotlinx.coroutines.flow.Flow

/**
 * 내려받을 모델 배포물 하나.
 *
 * @property fileName 저장될 파일명. 캐시 키의 모델 버전에도 들어간다(F-021).
 * @property sizeBytes 기대 크기. 다 받았는지 판단하고 진행률을 계산한다.
 */
data class LlmModelRelease(
    val fileName: String,
    val url: String,
    val sizeBytes: Long,
)

/** 다운로드 진행 상태. */
sealed interface ModelDownloadState {
    data class Running(
        val downloadedBytes: Long,
        val totalBytes: Long,
    ) : ModelDownloadState {
        val fraction: Float
            get() = if (totalBytes <= 0L) 0f else (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
    }

    data class Completed(
        val file: LlmModelFile,
    ) : ModelDownloadState
}

/**
 * 모델 파일 다운로드 포트.
 *
 * 2GB 짜리라 한 번에 끝나지 않는 경우가 흔하다. 어댑터는 받다 만 조각을 남겨 두고
 * 다음 호출에서 이어받는다. 취소는 코루틴 취소로 한다.
 */
interface LlmModelDownloader {
    /** 이 앱이 쓰는 배포물. 화면이 크기를 미리 보여 준다. */
    val release: LlmModelRelease

    fun download(): Flow<ModelDownloadState>
}

/** 자동 시작을 판단할 네트워크 상태 포트. 2GB 를 셀룰러로 말없이 받지 않기 위해 있다. */
interface NetworkStatus {
    suspend fun isUnmetered(): Boolean
}

/** 다운로드가 끝까지 가지 못했을 때. */
class ModelDownloadException(
    message: String,
) : IllegalStateException(message)
