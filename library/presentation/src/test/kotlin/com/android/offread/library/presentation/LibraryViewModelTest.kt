package com.android.offread.library.presentation

import com.android.offread.library.domain.model.LibrarySort
import com.android.offread.library.domain.usecase.CreateCollectionUseCase
import com.android.offread.library.domain.usecase.DeleteCollectionUseCase
import com.android.offread.library.domain.usecase.ObserveCollectionsUseCase
import com.android.offread.library.domain.usecase.RenameCollectionUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LibraryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(repo: FakeLibraryRepository = FakeLibraryRepository()) =
        LibraryViewModel(
            ObserveCollectionsUseCase(repo),
            CreateCollectionUseCase(repo),
            RenameCollectionUseCase(repo),
            DeleteCollectionUseCase(repo),
        )

    @Test
    fun `초기에는 빈 상태다`() {
        val state = viewModel().uiState.value

        assertTrue(state.collections.isEmpty())
        assertTrue(state.isEmpty)
    }

    @Test
    fun `컬렉션을 생성하면 목록에 반영되고 다이얼로그가 닫힌다`() {
        val vm = viewModel()

        vm.onIntent(LibraryIntent.AddCollectionClicked)
        assertEquals(CollectionDialog.Create, vm.uiState.value.dialog)

        vm.onIntent(LibraryIntent.SubmitCreate("이세계물"))

        assertNull(vm.uiState.value.dialog)
        assertEquals(
            listOf("이세계물"),
            vm.uiState.value.collections
                .map { it.name },
        )
        assertFalse(vm.uiState.value.isEmpty)
    }

    @Test
    fun `이름 변경이 반영된다`() {
        val vm = viewModel()
        vm.onIntent(LibraryIntent.SubmitCreate("old"))
        val id =
            vm.uiState.value.collections
                .single()
                .id

        vm.onIntent(LibraryIntent.SubmitRename(id, "new"))

        assertEquals(
            "new",
            vm.uiState.value.collections
                .single()
                .name,
        )
    }

    @Test
    fun `삭제가 반영된다`() {
        val vm = viewModel()
        vm.onIntent(LibraryIntent.SubmitCreate("temp"))
        val id =
            vm.uiState.value.collections
                .single()
                .id

        vm.onIntent(LibraryIntent.ConfirmDelete(id))

        assertTrue(
            vm.uiState.value.collections
                .isEmpty(),
        )
    }

    @Test
    fun `정렬을 이름순으로 바꾸면 목록이 재정렬된다`() {
        val vm = viewModel()
        vm.onIntent(LibraryIntent.SubmitCreate("나"))
        vm.onIntent(LibraryIntent.SubmitCreate("가"))

        vm.onIntent(LibraryIntent.ChangeSort(LibrarySort.NAME))

        assertEquals(
            listOf("가", "나"),
            vm.uiState.value.collections
                .map { it.name },
        )
        assertEquals(LibrarySort.NAME, vm.uiState.value.sort)
    }

    @Test
    fun `공백 이름 생성은 다이얼로그를 닫지 않는다`() {
        val vm = viewModel()
        vm.onIntent(LibraryIntent.AddCollectionClicked)

        vm.onIntent(LibraryIntent.SubmitCreate("   "))

        assertEquals(CollectionDialog.Create, vm.uiState.value.dialog)
        assertTrue(
            vm.uiState.value.collections
                .isEmpty(),
        )
    }
}
