package com.android.offread.importer.data

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebNovelImporterImplTest {
    private val importer = WebNovelImporterImpl()

    @Test
    fun `사이트를 가리지 않고 http https 주소를 받는다`() {
        assertTrue(importer.isSupported("https://ncode.syosetu.com/n9669bk/"))
        assertTrue(importer.isSupported("https://kakuyomu.jp/works/1177354054880848050"))
        assertTrue(importer.isSupported("http://example.com/novel/1"))
    }

    @Test
    fun `주소 형태가 아니면 받지 않는다`() {
        assertFalse(importer.isSupported("무직전생"))
        assertFalse(importer.isSupported("ftp://example.com/x"))
        assertFalse(importer.isSupported("https://"))
        assertFalse(importer.isSupported(""))
    }

    @Test
    fun `앞뒤 공백이 있어도 받아들인다`() {
        assertTrue(importer.isSupported("  https://example.com/novel  "))
    }

    @Test
    fun `알려진 사이트는 한국어 표기로 보여준다`() =
        runTest {
            assertEquals("소설가가 되자", importer.recognize("https://ncode.syosetu.com/n9669bk/").siteName)
            assertEquals("카쿠요무", importer.recognize("https://kakuyomu.jp/works/1").siteName)
        }

    @Test
    fun `그 밖의 사이트는 호스트를 그대로 보여준다`() =
        runTest {
            assertEquals("example.com", importer.recognize("https://www.example.com/novel/1").siteName)
        }
}
