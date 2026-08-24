package com.android.offread.translate.domain

import com.android.offread.core.entity.LanguagePair
import org.junit.Assert.assertEquals
import org.junit.Test

class TranslateGemmaPromptBuilderTest {
    private val builder = TranslateGemmaPromptBuilder()

    @Test
    fun `학습된 태그 형식으로 만든다`() {
        val prompt = builder.build("ルーデウスは歩いた。", LanguagePair.JA_KO)

        assertEquals("<src>ja</src><dst>ko</dst><text>ルーデウスは歩いた。</text>", prompt)
    }

    @Test
    fun `언어쌍마다 ISO 코드가 바뀐다`() {
        assertEquals(
            "<src>zh</src><dst>ko</dst><text>你好</text>",
            builder.build("你好", LanguagePair.ZH_KO),
        )
        assertEquals(
            "<src>en</src><dst>ko</dst><text>hello</text>",
            builder.build("hello", LanguagePair.EN_KO),
        )
    }

    @Test
    fun `응답에 태그가 섞여 나와도 걷어낸다`() {
        assertEquals("루데우스는 걸었다.", builder.clean("<text>루데우스는 걸었다.</text>"))
        assertEquals("루데우스는 걸었다.", builder.clean("  루데우스는 걸었다.  "))
    }

    @Test
    fun `따옴표와 낫표를 벗긴다`() {
        assertEquals("루데우스는 걸었다.", builder.clean("\"루데우스는 걸었다.\""))
        assertEquals("루데우스는 걸었다.", builder.clean("「루데우스는 걸었다.」"))
    }
}
