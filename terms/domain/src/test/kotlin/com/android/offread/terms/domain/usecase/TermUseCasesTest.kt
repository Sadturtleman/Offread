package com.android.offread.terms.domain.usecase

import com.android.offread.terms.domain.FakeTermRepository
import com.android.offread.terms.domain.model.Term
import com.android.offread.terms.domain.model.TermFilter
import com.android.offread.terms.domain.model.TermOrigin
import com.android.offread.terms.domain.model.TermStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TermUseCasesTest {
    private fun term(
        id: String,
        origin: TermOrigin = TermOrigin.MANUAL,
        pinned: Boolean = false,
    ) = Term(id, "c0", "原", "번역", pinned, origin, 0, TermStatus.CONFIRMED, 0)

    @Test
    fun `용어를 수동으로 추가한다`() =
        runTest {
            val repo = FakeTermRepository()
            val result =
                UpsertTermUseCase(repo)(
                    UpsertTermUseCase.Input("c0", "ソフィア", "소피아", pinned = true),
                )

            assertTrue(result.isSuccess)
            val saved = repo.current().single()
            assertEquals("소피아", saved.translation)
            assertEquals(TermOrigin.MANUAL, saved.origin)
            assertTrue(saved.pinned)
        }

    @Test
    fun `원어나 번역이 공백이면 실패한다`() =
        runTest {
            val repo = FakeTermRepository()
            val result = UpsertTermUseCase(repo)(UpsertTermUseCase.Input("c0", "  ", "소피아", pinned = false))

            assertTrue(result.exceptionOrNull() is BlankTermException)
            assertTrue(repo.current().isEmpty())
        }

    @Test
    fun `기존 용어를 수정하면 id 가 유지된다`() =
        runTest {
            val repo = FakeTermRepository()
            val existing = term("t9")
            repo.seed(existing)

            UpsertTermUseCase(repo)(
                UpsertTermUseCase.Input("c0", "原", "새번역", pinned = true, existing = existing),
            )

            val updated = repo.current().single()
            assertEquals("t9", updated.id)
            assertEquals("새번역", updated.translation)
            assertTrue(updated.pinned)
        }

    @Test
    fun `필터는 고정 용어만 남긴다`() =
        runTest {
            val repo = FakeTermRepository()
            repo.seed(term("t1", origin = TermOrigin.AUTO, pinned = true))
            repo.seed(term("t2", origin = TermOrigin.MANUAL, pinned = false))

            val pinned = ObserveTermsUseCase(repo)("c0", TermFilter.PINNED).first()

            assertEquals(listOf("t1"), pinned.map { it.id })
        }

    @Test
    fun `자동 제안 수락은 상태를 확정으로 바꾼다`() =
        runTest {
            val repo = FakeTermRepository()
            val suggested = term("t5").copy(origin = TermOrigin.AUTO, status = TermStatus.SUGGESTED)
            repo.seed(suggested)

            AcceptSuggestionUseCase(repo)(suggested)

            assertEquals(TermStatus.CONFIRMED, repo.current().single().status)
        }
}
