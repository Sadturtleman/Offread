package com.android.offread.translate.presentation

import com.android.offread.translate.domain.SegmentSplitter
import com.android.offread.translate.domain.TranslationEngineUnavailableException
import com.android.offread.translate.domain.model.TranslationEngineKind
import com.android.offread.translate.domain.model.VisibleText
import com.android.offread.translate.domain.usecase.TranslateTextsUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
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

    private val texts = listOf(VisibleText("0", "첫 노드."), VisibleText("1", "둘째 노드."))

    private fun viewModel(
        engine: FakeTranslationEngine = FakeTranslationEngine(),
        downloader: FakeLlmModelDownloader = FakeLlmModelDownloader(),
        networkStatus: FakeNetworkStatus = FakeNetworkStatus(unmetered = false),
    ) = TranslateViewModel(
        TranslateTextsUseCase(SegmentSplitter(), engine, cache),
        preference,
        modelStore,
        cache,
        downloader,
        networkStatus,
    )

    @Test
    fun `주소를 넣고 번역하면 웹뷰에 그 주소를 띄운다`() {
        val vm = viewModel()
        vm.onIntent(TranslateIntent.UrlChanged("https://example.com/novel/1"))

        vm.onIntent(TranslateIntent.Translate)

        assertEquals("https://example.com/novel/1", vm.uiState.value.loadedUrl)
        assertTrue(vm.uiState.value.loading)
    }

    @Test
    fun `스킴을 빼먹어도 주소로 연다`() {
        val vm = viewModel()
        vm.onIntent(TranslateIntent.UrlChanged("example.com/novel/1"))

        vm.onIntent(TranslateIntent.Translate)

        assertEquals("https://example.com/novel/1", vm.uiState.value.loadedUrl)
    }

    @Test
    fun `주소가 비면 열지 않는다`() {
        val vm = viewModel()

        vm.onIntent(TranslateIntent.Translate)

        assertNull(vm.uiState.value.loadedUrl)
        assertFalse(vm.uiState.value.canTranslate)
    }

    @Test
    fun `모아 온 노드를 번역해 제자리에 돌려보낸다`() =
        runTest {
            val vm = viewModel()

            vm.onIntent(TranslateIntent.TextsCollected(texts))

            val applied =
                vm.effect
                    .take(texts.size)
                    .toList()
                    .filterIsInstance<TranslateEffect.ApplyTranslation>()
            assertEquals(listOf("0", "1"), applied.map { it.id })
            assertEquals(listOf("번역:첫 노드.", "번역:둘째 노드."), applied.map { it.text })
            assertEquals(2, vm.uiState.value.translated)
            assertFalse(vm.uiState.value.translating)
        }

    @Test
    fun `실패한 노드를 세고 다시 시도할 수 있다`() {
        val engine = FakeTranslationEngine(failFor = setOf("둘째 노드."))
        val vm = viewModel(engine = engine)
        vm.onIntent(TranslateIntent.TextsCollected(texts))
        assertEquals(1, vm.uiState.value.failed)

        vm.onIntent(TranslateIntent.RetryFailed)

        // 실패한 노드만 다시 돌린다 — 성공한 것까지 또 추론하지 않는다.
        assertEquals(listOf("첫 노드.", "둘째 노드.", "둘째 노드."), engine.translatedTexts)
    }

    @Test
    fun `번역할 일본어가 없으면 아무것도 세지 않는다`() {
        val vm = viewModel()

        vm.onIntent(TranslateIntent.TextsCollected(emptyList()))

        assertEquals(0, vm.uiState.value.total)
        assertFalse(vm.uiState.value.translating)
    }

    @Test
    fun `모델 파일이 없으면 이유를 메시지로 알린다`() =
        runTest {
            val engine = FakeTranslationEngine(error = TranslationEngineUnavailableException("모델 파일이 없어요."))
            val vm = viewModel(engine = engine)

            vm.onIntent(TranslateIntent.TextsCollected(texts))

            assertEquals(
                "모델 파일이 없어요.",
                (vm.effect.first() as TranslateEffect.ShowMessage).message,
            )
        }

    @Test
    fun `모델이 없고 Wi-Fi 면 실행하자마자 받는다`() {
        val downloader = FakeLlmModelDownloader(store = modelStore)

        val vm = viewModel(downloader = downloader, networkStatus = FakeNetworkStatus(unmetered = true))

        assertEquals(1, downloader.started)
        assertEquals(
            listOf(downloader.release.fileName),
            vm.uiState.value.models
                .map { it.name },
        )
        assertFalse(vm.uiState.value.modelMissing)
    }

    @Test
    fun `종량제 망에서는 말없이 받지 않는다`() {
        val downloader = FakeLlmModelDownloader(store = modelStore)

        val vm = viewModel(downloader = downloader, networkStatus = FakeNetworkStatus(unmetered = false))

        assertEquals(0, downloader.started)
        assertTrue(vm.uiState.value.modelMissing)
        assertEquals(downloader.release.sizeBytes, vm.uiState.value.modelSizeBytes)
    }

    @Test
    fun `직접 누르면 종량제 망에서도 받는다`() {
        val downloader = FakeLlmModelDownloader(store = modelStore)
        val vm = viewModel(downloader = downloader, networkStatus = FakeNetworkStatus(unmetered = false))

        vm.onIntent(TranslateIntent.DownloadModel)

        assertEquals(1, downloader.started)
        assertNull(vm.uiState.value.download)
        assertFalse(vm.uiState.value.modelMissing)
    }

    @Test
    fun `다운로드가 실패하면 이유를 알린다`() =
        runTest {
            val downloader = FakeLlmModelDownloader(error = IllegalStateException("모델 서버가 응답하지 않아요."))
            val vm = viewModel(downloader = downloader)

            vm.onIntent(TranslateIntent.DownloadModel)

            assertEquals(
                "모델 서버가 응답하지 않아요.",
                (vm.effect.first() as TranslateEffect.ShowMessage).message,
            )
            assertNull(vm.uiState.value.download)
        }

    @Test
    fun `기본 엔진은 TranslateGemma 다`() {
        assertEquals(TranslationEngineKind.TRANSLATE_GEMMA, viewModel().uiState.value.engine)
    }

    @Test
    fun `엔진을 바꾸면 상태에 반영된다`() {
        val vm = viewModel()

        vm.onIntent(TranslateIntent.SelectEngine(TranslationEngineKind.ML_KIT))

        assertEquals(TranslationEngineKind.ML_KIT, vm.uiState.value.engine)
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
        vm.onIntent(TranslateIntent.TextsCollected(texts))
        assertTrue(vm.uiState.value.cache.entryCount > 0)

        vm.onIntent(TranslateIntent.ClearCache)

        assertTrue(cache.cleared)
        assertEquals(0, vm.uiState.value.cache.entryCount)
    }
}
