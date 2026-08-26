package com.android.offread.translate.presentation

import com.android.offread.translate.domain.SegmentSplitter
import com.android.offread.translate.domain.model.TranslationEngineKind
import com.android.offread.translate.domain.usecase.TranslatePageUseCase
import com.android.offread.translate.domain.usecase.TranslateSegmentUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TranslateViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val cache = FakeSegmentCache()
    private val preference = FakeEnginePreference()
    private val modelStore = FakeLlmModelStore()

    private fun viewModel(
        engine: FakeTranslationEngine = FakeTranslationEngine(),
        source: FakeWebPageSource = FakeWebPageSource(),
    ) = TranslateViewModel(
        TranslatePageUseCase(source, SegmentSplitter(), engine, cache),
        TranslateSegmentUseCase(engine, cache),
        preference,
        modelStore,
        cache,
    )

    @Test
    fun `주소를 넣고 번역하면 문단이 채워진다`() {
        val vm = viewModel()
        vm.onIntent(TranslateIntent.UrlChanged("https://example.com/novel/1"))

        vm.onIntent(TranslateIntent.Translate)

        val page = vm.uiState.value.page
        assertEquals("제목", page?.title)
        assertEquals(listOf("번역:첫 문단.", "번역:둘째 문단."), page?.segments?.map { it.translated })
        assertFalse(vm.uiState.value.loading)
    }

    @Test
    fun `주소가 비면 번역하지 않는다`() {
        val vm = viewModel()

        vm.onIntent(TranslateIntent.Translate)

        assertNull(vm.uiState.value.page)
        assertFalse(vm.uiState.value.canTranslate)
    }

    @Test
    fun `수집에 실패하면 메시지로 알린다`() =
        runTest {
            val vm = viewModel(source = FakeWebPageSource(error = IllegalStateException("페이지를 가져오지 못했어요.")))
            vm.onIntent(TranslateIntent.UrlChanged("https://example.com/x"))

            vm.onIntent(TranslateIntent.Translate)

            assertEquals(
                "페이지를 가져오지 못했어요.",
                (vm.effect.first() as TranslateEffect.ShowMessage).message,
            )
            assertNull(vm.uiState.value.page)
        }

    @Test
    fun `번역에 실패한 문단은 재시도로 채운다`() {
        val failing = FakeTranslationEngine(error = IllegalStateException("모델 없음"))
        val vm =
            TranslateViewModel(
                TranslatePageUseCase(FakeWebPageSource(), SegmentSplitter(), failing, cache),
                TranslateSegmentUseCase(FakeTranslationEngine(), cache),
                preference,
                modelStore,
                cache,
            )
        vm.onIntent(TranslateIntent.UrlChanged("https://example.com/x"))
        vm.onIntent(TranslateIntent.Translate)
        assertTrue(
            vm.uiState.value.page
                ?.segments
                ?.all { it.translated == null } == true,
        )

        vm.onIntent(TranslateIntent.RetrySegment("seg-1"))

        assertEquals(
            "번역:첫 문단.",
            vm.uiState.value.page
                ?.segments
                ?.first()
                ?.translated,
        )
        assertNull(vm.uiState.value.retryingSegmentId)
    }

    @Test
    fun `엔진을 바꾸면 상태에 반영된다`() {
        val vm = viewModel()

        vm.onIntent(TranslateIntent.SelectEngine(TranslationEngineKind.TRANSLATE_GEMMA))

        assertEquals(TranslationEngineKind.TRANSLATE_GEMMA, vm.uiState.value.engine)
    }

    @Test
    fun `모델 파일을 가져오고 지운다`() {
        val vm = viewModel()

        vm.onIntent(TranslateIntent.ImportModel("content://docs/translategemma-4b.litertlm"))
        assertEquals(
            listOf("translategemma-4b.litertlm"),
            vm.uiState.value.models
                .map { it.name },
        )

        vm.onIntent(TranslateIntent.DeleteModel("translategemma-4b.litertlm"))
        assertTrue(
            vm.uiState.value.models
                .isEmpty(),
        )
    }

    @Test
    fun `캐시를 비우면 사용량이 0 이 된다`() {
        val vm = viewModel()
        vm.onIntent(TranslateIntent.UrlChanged("https://example.com/x"))
        vm.onIntent(TranslateIntent.Translate)
        assertTrue(vm.uiState.value.cache.entryCount > 0)

        vm.onIntent(TranslateIntent.ClearCache)

        assertTrue(cache.cleared)
        assertEquals(0, vm.uiState.value.cache.entryCount)
    }
}
