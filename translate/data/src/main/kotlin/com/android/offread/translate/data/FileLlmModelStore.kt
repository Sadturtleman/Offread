package com.android.offread.translate.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.android.offread.translate.domain.LlmModelFile
import com.android.offread.translate.domain.LlmModelStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [LlmModelStore] 어댑터. 앱 전용 저장소(files/llm)에만 둔다(P-01: 외부·공용 저장소 미사용).
 * 모델은 수 GB 이므로 복사는 IO 디스패처에서 스트리밍으로 한다.
 */
@Singleton
class FileLlmModelStore
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : LlmModelStore {
        private val directory by lazy {
            LlmModelDirectory(File(context.filesDir, LlmModelDirectory.DIR_NAME).apply { mkdirs() })
        }

        override suspend fun installed(): List<LlmModelFile> = withContext(Dispatchers.IO) { directory.list() }

        override suspend fun import(uri: String): LlmModelFile =
            withContext(Dispatchers.IO) {
                val parsed = Uri.parse(uri)
                val name = LlmModelDirectory.sanitize(displayNameOf(parsed) ?: parsed.lastPathSegment.orEmpty())
                val runtime =
                    LlmModelDirectory.runtimeOf(name)
                        ?: throw UnsupportedModelFileException(name)
                directory.ensureRoot()
                val target = directory.fileOf(name)
                context.contentResolver.openInputStream(parsed)?.use { input ->
                    target.outputStream().use { output -> input.copyTo(output, DEFAULT_BUFFER_BYTES) }
                } ?: throw IllegalStateException("파일을 열 수 없어요.")
                LlmModelFile(name = name, sizeBytes = target.length(), runtime = runtime)
            }

        override suspend fun delete(name: String) {
            withContext(Dispatchers.IO) { directory.delete(name) }
        }

        /** SAF 문서의 표시 이름. 확장자로 런타임을 판별하므로 원래 이름이 필요하다. */
        private fun displayNameOf(uri: Uri): String? =
            context.contentResolver
                .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }

        private companion object {
            const val DEFAULT_BUFFER_BYTES = 1 shl 20
        }
    }

/** 지원하지 않는 확장자. */
class UnsupportedModelFileException(
    fileName: String,
) : IllegalArgumentException("$fileName 은 지원하지 않는 형식이에요. .task 또는 .litertlm 파일이 필요해요.")
