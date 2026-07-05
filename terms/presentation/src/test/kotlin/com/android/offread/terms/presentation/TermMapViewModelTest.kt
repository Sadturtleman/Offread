package com.android.offread.terms.presentation

import com.android.offread.terms.domain.model.Term
import com.android.offread.terms.domain.model.TermFilter
import com.android.offread.terms.domain.model.TermOrigin
import com.android.offread.terms.domain.model.TermStatus
import com.android.offread.terms.domain.usecase.AcceptSuggestionUseCase
import com.android.offread.terms.domain.usecase.DeleteTermUseCase
import com.android.offread.terms.domain.usecase.ObserveTermsUseCase
import com.android.offread.terms.domain.usecase.RejectSuggestionUseCase
import com.android.offread.terms.domain.usecase.UpsertTermUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TermMapViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(repo: FakeTermRepository): TermMapViewModel =
        TermMapViewModel(
            ObserveTermsUseCase(repo),
            UpsertTermUseCase(repo),
            DeleteTermUseCase(repo),
            AcceptSuggestionUseCase(repo),
            RejectSuggestionUseCase(repo),
        ).also { it.start("c0") }

    private fun term(
        id: String,
        pinned: Boolean = false,
        origin: TermOrigin = TermOrigin.MANUAL,
    ) = Term(id, "c0", "原$id", "번역$id", pinned, origin, 0, TermStatus.CONFIRMED, 0)

    @Test
    fun `용어를 추가하면 목록에 반영되고 다이얼로그가 닫힌다`() {
        val repo = FakeTermRepository()
        val vm = viewModel(repo)

        vm.onIntent(TermMapIntent.AddClicked)
        assertEquals(TermDialog.Add, vm.uiState.value.dialog)

        vm.onIntent(TermMapIntent.Submit("ソフィア", "소피아", pinned = true))

        assertNull(vm.uiState.value.dialog)
        assertEquals(
            "소피아",
            vm.uiState.value.terms
                .single()
                .translation,
        )
    }

    @Test
    fun `공백 입력은 다이얼로그를 닫지 않는다`() {
        val repo = FakeTermRepository()
        val vm = viewModel(repo)
        vm.onIntent(TermMapIntent.AddClicked)

        vm.onIntent(TermMapIntent.Submit("  ", "소피아", pinned = false))

        assertEquals(TermDialog.Add, vm.uiState.value.dialog)
        assertTrue(
            vm.uiState.value.terms
                .isEmpty(),
        )
    }

    @Test
    fun `삭제가 반영된다`() {
        val repo = FakeTermRepository().apply { seed(term("t1")) }
        val vm = viewModel(repo)

        vm.onIntent(TermMapIntent.Delete("t1"))

        assertTrue(
            vm.uiState.value.terms
                .isEmpty(),
        )
    }

    @Test
    fun `고정 필터는 고정 용어만 노출한다`() {
        val repo =
            FakeTermRepository().apply {
                seed(term("t1", pinned = true))
                seed(term("t2", pinned = false))
            }
        val vm = viewModel(repo)

        vm.onIntent(TermMapIntent.ChangeFilter(TermFilter.PINNED))

        assertEquals(
            listOf("t1"),
            vm.uiState.value.terms
                .map { it.id },
        )
        assertEquals(TermFilter.PINNED, vm.uiState.value.filter)
    }

    @Test
    fun `제안 거절은 용어를 제거한다`() {
        val suggested = term("t9").copy(origin = TermOrigin.AUTO, status = TermStatus.SUGGESTED)
        val repo = FakeTermRepository().apply { seed(suggested) }
        val vm = viewModel(repo)

        vm.onIntent(TermMapIntent.RejectSuggestion(suggested))

        assertTrue(
            vm.uiState.value.terms
                .isEmpty(),
        )
    }
}
