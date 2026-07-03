package com.android.offread.library.domain.usecase

import com.android.offread.library.domain.FakeLibraryRepository
import com.android.offread.library.domain.model.LibrarySort
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CollectionCrudUseCasesTest {
    @Test
    fun `컬렉션을 생성한다`() =
        runTest {
            val repo = FakeLibraryRepository()
            val result = CreateCollectionUseCase(repo)("이세계물")

            assertTrue(result.isSuccess)
            assertEquals("이세계물", repo.current().single().name)
        }

    @Test
    fun `공백 이름은 생성에 실패한다`() =
        runTest {
            val repo = FakeLibraryRepository()
            val result = CreateCollectionUseCase(repo)("   ")

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is BlankCollectionNameException)
            assertTrue(repo.current().isEmpty())
        }

    @Test
    fun `이름을 변경한다`() =
        runTest {
            val repo = FakeLibraryRepository()
            val id = CreateCollectionUseCase(repo)("old").getOrThrow()

            val result = RenameCollectionUseCase(repo)(id, "new")

            assertTrue(result.isSuccess)
            assertEquals("new", repo.current().single().name)
        }

    @Test
    fun `삭제 시 하위 컬렉션도 함께 사라진다`() =
        runTest {
            val repo = FakeLibraryRepository()
            val parent = CreateCollectionUseCase(repo)("parent").getOrThrow()
            CreateCollectionUseCase(repo)("child", parent)

            DeleteCollectionUseCase(repo)(parent)

            assertTrue(repo.current().isEmpty())
        }

    @Test
    fun `이름순 정렬로 구독한다`() =
        runTest {
            val repo = FakeLibraryRepository()
            CreateCollectionUseCase(repo)("나")
            CreateCollectionUseCase(repo)("가")

            val names = ObserveCollectionsUseCase(repo)(LibrarySort.NAME).first().map { it.name }

            assertEquals(listOf("가", "나"), names)
        }
}
