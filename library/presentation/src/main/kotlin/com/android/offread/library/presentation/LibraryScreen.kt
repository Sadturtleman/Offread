package com.android.offread.library.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.offread.core.domain.navigation.AppRoutes
import com.android.offread.core.domain.navigation.NavRoute
import com.android.offread.core.entity.TranslationStatus
import com.android.offread.core.ui.helper.LocalMessageHelper
import com.android.offread.core.ui.helper.LocalNavigationHelper
import com.android.offread.library.domain.model.Collection
import com.android.offread.library.domain.model.LibraryItem
import com.android.offread.library.domain.model.LibrarySort

/**
 * L-01 라이브러리 홈(F-005) + 컬렉션 CRUD(F-006).
 */
@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val messageHelper = LocalMessageHelper.current
    val navigationHelper = LocalNavigationHelper.current

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LibraryEffect.ShowError -> messageHelper.showToast(effect.message)
                LibraryEffect.NavigateToImport -> navigationHelper.navigateByRoute(NavRoute(AppRoutes.IMPORT_SHEET))
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Text(
                text = "라이브러리",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "컬렉션", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                SortToggle(sort = state.sort, onChange = { viewModel.onIntent(LibraryIntent.ChangeSort(it)) })
            }

            if (state.isEmpty) {
                EmptyLibrary(
                    modifier = Modifier.weight(1f),
                    onImport = { viewModel.onIntent(LibraryIntent.ImportClicked) },
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.collections, key = { "c-${it.id}" }) { collection ->
                        CollectionRow(
                            collection = collection,
                            onRename = { viewModel.onIntent(LibraryIntent.RenameCollectionClicked(collection)) },
                            onDelete = { viewModel.onIntent(LibraryIntent.DeleteCollectionClicked(collection)) },
                        )
                    }
                    items(state.items, key = { "i-${it.id}" }) { item ->
                        ItemRow(item = item)
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { viewModel.onIntent(LibraryIntent.ImportClicked) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            text = { Text("가져오기") },
            icon = { Text("+") },
        )
    }

    CollectionDialogs(
        dialog = state.dialog,
        onDismiss = { viewModel.onIntent(LibraryIntent.DismissDialog) },
        onCreate = { viewModel.onIntent(LibraryIntent.SubmitCreate(it)) },
        onRename = { id, name -> viewModel.onIntent(LibraryIntent.SubmitRename(id, name)) },
        onConfirmDelete = { viewModel.onIntent(LibraryIntent.ConfirmDelete(it)) },
    )
}

@Composable
private fun SortToggle(
    sort: LibrarySort,
    onChange: (LibrarySort) -> Unit,
) {
    val next = if (sort == LibrarySort.RECENT) LibrarySort.NAME else LibrarySort.RECENT
    val label = if (sort == LibrarySort.RECENT) "최근 읽은 순" else "이름 순"
    TextButton(onClick = { onChange(next) }) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun CollectionRow(
    collection: Collection,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = collection.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(
                text = "작품 ${collection.itemCount} · 용어 ${collection.termCount}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onRename) { Text("이름 변경") }
                TextButton(onClick = onDelete) { Text("삭제") }
            }
        }
    }
}

@Composable
private fun ItemRow(
    item: LibraryItem,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                Text(text = item.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "웹소설 · ${item.author} · 전 ${item.totalChapters}화",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TranslationBadge(item.translationStatus)
        }
    }
}

@Composable
private fun TranslationBadge(status: TranslationStatus) {
    val label =
        when (status) {
            TranslationStatus.UNTRANSLATED -> "미번역"
            TranslationStatus.TRANSLATING -> "번역 중"
            TranslationStatus.CACHED -> "캐시됨"
            TranslationStatus.CLOUD_FALLBACK -> "폴백"
        }
    androidx.compose.material3.AssistChip(onClick = {}, enabled = false, label = { Text(label) })
}

@Composable
private fun EmptyLibrary(
    onImport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "아직 라이브러리가 비어 있어요.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = "원서를 가져와 오프라인 번역으로 읽어보세요.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
        TextButton(onClick = onImport, modifier = Modifier.padding(top = 12.dp)) {
            Text("가져오기")
        }
    }
}
