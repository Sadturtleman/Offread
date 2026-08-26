package com.android.offread.translate.data

import com.android.offread.translate.domain.LlmModelFile
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
            .filter { it.isFile && isModel(it.name) }
            .map { LlmModelFile(it.name, it.length()) }
            .sortedBy { it.name }

    /** 쓸 모델 파일. 여러 개면 가장 최근에 넣은 것. */
    fun latest(): File? =
        root
            .listFiles()
            .orEmpty()
            .filter { it.isFile && isModel(it.name) }
            .maxByOrNull { it.lastModified() }

    fun fileOf(name: String): File = File(root, sanitize(name))

    fun ensureRoot(): File = root.apply { mkdirs() }

    fun delete(name: String) {
        fileOf(name).delete()
    }

    companion object {
        const val DIR_NAME = "llm"
        const val EXTENSION = ".litertlm"

        /** TranslateGemma(LiteRT-LM) 번들만 받는다. */
        fun isModel(fileName: String): Boolean = fileName.endsWith(EXTENSION, ignoreCase = true)

        /** 사용자가 고른 파일명이 디렉터리를 벗어나지 않게 한다. */
        fun sanitize(displayName: String): String = displayName.substringAfterLast('/').substringAfterLast('\\')
    }
}
