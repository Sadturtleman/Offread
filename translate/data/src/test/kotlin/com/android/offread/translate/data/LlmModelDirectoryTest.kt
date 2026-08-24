package com.android.offread.translate.data

import com.android.offread.translate.domain.LlmRuntime
import org.junit.Assert.assertEquals
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
    fun `확장자로 런타임을 가른다`() {
        assertEquals(LlmRuntime.MEDIAPIPE_TASK, LlmModelDirectory.runtimeOf("gemma3-1b.task"))
        assertEquals(LlmRuntime.LITERT_LM, LlmModelDirectory.runtimeOf("translategemma-4b.litertlm"))
        assertNull(LlmModelDirectory.runtimeOf("readme.txt"))
    }

    @Test
    fun `모델이 아닌 파일은 목록에서 뺀다`() {
        seed("gemma3-1b.task")
        seed("메모.txt")

        assertEquals(listOf("gemma3-1b.task"), directory().list().map { it.name })
    }

    @Test
    fun `런타임별로 가장 최근 파일을 고른다`() {
        seed("old.litertlm", lastModified = 1_000)
        seed("new.litertlm", lastModified = 9_000)
        seed("task-model.task", lastModified = 5_000)

        assertEquals("new.litertlm", directory().latest(LlmRuntime.LITERT_LM)?.name)
        assertEquals("task-model.task", directory().latest(LlmRuntime.MEDIAPIPE_TASK)?.name)
    }

    @Test
    fun `해당 런타임 파일이 없으면 null 이다`() {
        seed("only.task")

        assertNull(directory().latest(LlmRuntime.LITERT_LM))
    }

    @Test
    fun `파일명에 섞인 경로는 잘라내 디렉터리를 벗어나지 않게 한다`() {
        assertEquals("model.litertlm", LlmModelDirectory.sanitize("../../etc/model.litertlm"))
        assertEquals("model.task", LlmModelDirectory.sanitize("C:\\models\\model.task"))
    }

    @Test
    fun `삭제하면 목록에서 사라진다`() {
        seed("gemma3-1b.task")

        directory().delete("gemma3-1b.task")

        assertTrue(directory().list().isEmpty())
    }
}
