package com.android.offread.translate.data

import com.android.offread.translate.domain.LlmModelFile
import com.android.offread.translate.domain.LlmRuntime
import java.io.File

/**
 * 모델 파일 디렉터리 규칙. 파일 시스템만 다루므로 안드로이드 의존이 없고 단위 테스트가 된다.
 */
internal class LlmModelDirectory(
    private val root: File,
) {
    fun list(): List<LlmModelFile> =
        root
            .listFiles()
            .orEmpty()
            .filter { it.isFile }
            .mapNotNull { file -> runtimeOf(file.name)?.let { LlmModelFile(file.name, file.length(), it) } }
            .sortedBy { it.name }

    /** 해당 런타임에서 쓸 파일. 여러 개면 가장 최근에 넣은 것. */
    fun latest(runtime: LlmRuntime): File? =
        root
            .listFiles()
            .orEmpty()
            .filter { it.isFile && runtimeOf(it.name) == runtime }
            .maxByOrNull { it.lastModified() }

    fun fileOf(name: String): File = File(root, sanitize(name))

    fun ensureRoot(): File = root.apply { mkdirs() }

    fun delete(name: String) {
        fileOf(name).delete()
    }

    companion object {
        const val DIR_NAME = "llm"

        fun runtimeOf(fileName: String): LlmRuntime? =
            when {
                fileName.endsWith(".task", ignoreCase = true) -> LlmRuntime.MEDIAPIPE_TASK
                fileName.endsWith(".litertlm", ignoreCase = true) -> LlmRuntime.LITERT_LM
                else -> null
            }

        /** 사용자가 고른 파일명이 디렉터리를 벗어나지 않게 한다. */
        fun sanitize(displayName: String): String = displayName.substringAfterLast('/').substringAfterLast('\\')
    }
}
