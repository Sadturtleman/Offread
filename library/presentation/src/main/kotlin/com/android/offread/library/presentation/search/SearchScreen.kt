package com.android.offread.library.presentation.search

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.offread.core.entity.ItemType
import com.android.offread.core.ui.helper.LocalNavigationHelper
import com.android.offread.core.ui.helper.singleClickable
import com.android.offread.library.domain.WebNovelDetailPage
import com.android.offread.library.domain.model.Collection
import com.android.offread.library.domain.model.LibraryItem

/**
 * L-05 검색(F-010). 제목·작가 로컬 검색 + 컬렉션·유형 필터.
 *
 * @param collectionId 컬렉션 내 검색으로 진입할 때의 초기 스코프(null 이면 전역)
 */
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    collectionId: String? = null,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navigationHelper = LocalNavigationHelper.current

    LaunchedEffect(collectionId) { viewModel.start(collectionId) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Text(text = "검색", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = state.text,
                onValueChange = { viewModel.onIntent(SearchIntent.ChangeText(it)) },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                singleLine = true,
                label = { Text("제목 · 작가") },
                trailingIcon = {
                    if (state.text.isNotEmpty()) {
                        TextButton(onClick = { viewModel.onIntent(SearchIntent.ClearText) }) { Text("지우기") }
                    }
                },
            )

            CollectionFilterRow(
                collections = state.collections,
                selectedId = state.collectionId,
                onSelect = { viewModel.onIntent(SearchIntent.ChangeCollection(it)) },
            )

            TypeFilterRow(
                selected = state.type,
                onSelect = { viewModel.onIntent(SearchIntent.ChangeType(it)) },
            )

            when {
                !state.searched ->
                    Hint(
                        text = "제목이나 작가를 입력하면 기기 안에서 바로 찾아요.",
                        modifier = Modifier.weight(1f),
                    )
                state.results.isEmpty() ->
                    Hint(
                        text = "\"${state.text}\" 와 맞는 작품이 없어요.",
                        modifier = Modifier.weight(1f),
                    )
                else ->
                    LazyColumn(
                        modifier = Modifier.weight(1f).padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.results, key = { it.id }) { item ->
                            ResultRow(
                                item = item,
                                onClick = { navigationHelper.navigateTo(WebNovelDetailPage(item.id)) },
                            )
                        }
                    }
            }
        }
    }
}

@Composable
private fun CollectionFilterRow(
    collections: List<Collection>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 12.dp).horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selectedId == null,
            onClick = { onSelect(null) },
            label = { Text("전체 컬렉션") },
        )
        collections.forEach { collection ->
            FilterChip(
                selected = selectedId == collection.id,
                onClick = { onSelect(collection.id) },
                label = { Text(collection.name) },
            )
        }
    }
}

@Composable
private fun TypeFilterRow(
    selected: ItemType?,
    onSelect: (ItemType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf(null, ItemType.WEBNOVEL, ItemType.PAPER).forEach { type ->
            FilterChip(
                selected = selected == type,
                onClick = { onSelect(type) },
                label = { Text(type.label()) },
            )
        }
    }
}

@Composable
private fun ResultRow(
    item: LibraryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().singleClickable(onClick = onClick).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(text = item.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(
                text = "${item.type.label()} · ${item.author} · ${item.siteName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Hint(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
