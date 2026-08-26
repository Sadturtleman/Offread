package com.android.offread.translate.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class LlmModelDirectoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private fun directory() = LlmModelDirectory(temporaryFolder.root)

    private fun seed(
        name: String,
        lastModified: Long = 1_000,
    ) = temporaryFolder.newFile(name).apply {
        writeText("model")
        setLastModified(lastModified)
    }

    @Test
    fun `litertlm 파일만 모델로 본다`() {
        assertTrue(LlmModelDirectory.isModel("translategemma-4b.litertlm"))
        assertFalse(LlmModelDirectory.isModel("gemma3-1b.task"))
        assertFalse(LlmModelDirectory.isModel("readme.txt"))
    }

    @Test
    fun `모델이 아닌 파일은 목록에서 뺀다`() {
        seed("translategemma-4b.litertlm")
        seed("메모.txt")

        assertEquals(listOf("translategemma-4b.litertlm"), directory().list().map { it.name })
    }

    @Test
    fun `여러 개면 가장 최근 파일을 쓴다`() {
        seed("old.litertlm", lastModified = 1_000)
        seed("new.litertlm", lastModified = 9_000)

        assertEquals("new.litertlm", directory().latest()?.name)
    }

    @Test
    fun `모델이 없으면 null 이다`() {
        seed("메모.txt")

        assertNull(directory().latest())
    }

    @Test
    fun `파일명에 섞인 경로는 잘라내 디렉터리를 벗어나지 않게 한다`() {
        assertEquals("model.litertlm", LlmModelDirectory.sanitize("../../etc/model.litertlm"))
        assertEquals("model.litertlm", LlmModelDirectory.sanitize("C:\\models\\model.litertlm"))
    }

    @Test
    fun `삭제하면 목록에서 사라진다`() {
        seed("translategemma-4b.litertlm")

        directory().delete("translategemma-4b.litertlm")

        assertTrue(directory().list().isEmpty())
    }
}
