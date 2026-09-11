package com.android.offread.translate.data

import android.content.Context
import com.android.offread.translate.domain.LlmModelDownloader
import com.android.offread.translate.domain.LlmModelFile
import com.android.offread.translate.domain.LlmModelRelease
import com.android.offread.translate.domain.ModelDownloadException
import com.android.offread.translate.domain.ModelDownloadState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hugging Face 의 공개 변환본을 그대로 내려받는다(#57).
 *
 * 파일이 2GB 라 중간에 끊기는 걸 전제로 짰다 — 받은 만큼 `.part` 로 남기고 다음 호출에서
 * `Range` 헤더로 이어받는다. 서버가 이어받기를 거절하면(200 응답) 조각을 버리고 처음부터 받는다.
 * 다 받은 뒤에야 최종 파일명으로 바꾸므로, 반쯤 받은 파일을 엔진이 집어 드는 일은 없다.
 */
@Singleton
class HttpLlmModelDownloader
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : LlmModelDownloader {
        override val release: LlmModelRelease = TRANSLATE_GEMMA_INT4

        private val directory by lazy {
            LlmModelDirectory(File(context.filesDir, LlmModelDirectory.DIR_NAME))
        }

        override fun download(): Flow<ModelDownloadState> =
            flow {
                directory.ensureRoot()
                val target = directory.fileOf(release.fileName)
                if (target.length() == release.sizeBytes) {
                    emit(ModelDownloadState.Completed(LlmModelFile(target.name, target.length())))
                    return@flow
                }
                val part = File(target.parentFile, target.name + PART_SUFFIX)
                emit(ModelDownloadState.Running(part.length(), release.sizeBytes))
                fetch(part)
                if (part.length() != release.sizeBytes) {
                    throw ModelDownloadException("모델을 끝까지 받지 못했어요. 다시 시도해 주세요.")
                }
                target.delete()
                if (!part.renameTo(target)) throw ModelDownloadException("받은 파일을 저장하지 못했어요.")
                emit(ModelDownloadState.Completed(LlmModelFile(target.name, target.length())))
            }.flowOn(Dispatchers.IO)

        private suspend fun FlowCollector<ModelDownloadState>.fetch(part: File) {
            var downloaded = part.length()
            val connection = open(from = downloaded)
            try {
                val resumed = connection.responseCode == HttpURLConnection.HTTP_PARTIAL
                if (!resumed) {
                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        throw ModelDownloadException("모델 서버가 응답하지 않아요(${connection.responseCode}).")
                    }
                    // 이어받기가 거절됐다. 조각을 버리고 처음부터.
                    part.delete()
                    downloaded = 0L
                }
                RandomAccessFile(part, "rw").use { output ->
                    output.seek(downloaded)
                    connection.inputStream.use { input ->
                        val buffer = ByteArray(BUFFER_BYTES)
                        var emittedAt = downloaded
                        while (true) {
                            currentCoroutineContext().ensureActive()
                            val read = input.read(buffer)
                            if (read < 0) break
                            output.write(buffer, 0, read)
                            downloaded += read
                            if (downloaded - emittedAt >= EMIT_EVERY_BYTES) {
                                emittedAt = downloaded
                                emit(ModelDownloadState.Running(downloaded, release.sizeBytes))
                            }
                        }
                    }
                }
            } finally {
                connection.disconnect()
            }
        }

        private fun open(from: Long): HttpURLConnection =
            (URL(release.url).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                connectTimeout = TIMEOUT_MILLIS
                readTimeout = TIMEOUT_MILLIS
                if (from > 0L) setRequestProperty("Range", "bytes=$from-")
            }

        private companion object {
            const val PART_SUFFIX = ".part"
            const val BUFFER_BYTES = 1 shl 16

            /** 진행률 방출 간격. 너무 촘촘하면 UI 가 재조립만 한다. */
            const val EMIT_EVERY_BYTES = 4L shl 20
            const val TIMEOUT_MILLIS = 30_000

            /**
             * 커뮤니티 변환본(INT4 generic). 게이트가 없어 토큰 없이 받을 수 있고 Range 요청을 받는다.
             * 파일명이나 경로가 바뀌면 여기만 고친다.
             */
            val TRANSLATE_GEMMA_INT4 =
                LlmModelRelease(
                    fileName = "translategemma-4b-it-int4-generic.litertlm",
                    url =
                        "https://huggingface.co/barakplasma/translategemma-4b-it-android-task-quantized/" +
                            "resolve/main/artifacts/int4-generic/translategemma-4b-it-int4-generic.litertlm",
                    sizeBytes = 2_011_201_536L,
                )
        }
    }
