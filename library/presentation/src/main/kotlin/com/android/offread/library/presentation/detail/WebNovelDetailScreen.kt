package com.android.offread.library.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.android.offread.core.entity.SerialStatus
import com.android.offread.core.entity.TranslationStatus
import com.android.offread.core.ui.helper.LocalMessageHelper
import com.android.offread.library.domain.model.Chapter
import com.android.offread.library.domain.model.LibraryItem

/**
 * L-03 웹소설 아이템 상세(F-008).
 */
@Composable
fun WebNovelDetailScreen(
    itemId: String,
    modifier: Modifier = Modifier,
    viewModel: WebNovelDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val messageHelper = LocalMessageHelper.current

    LaunchedEffect(itemId) { viewModel.start(itemId) }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WebNovelDetailEffect.ShowMessage -> messageHelper.showToast(effect.message)
            }
        }
    }

    val item = state.item ?: return

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = item.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = metaLine(item),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TranslationBadge(item.translationStatus)

                Button(
                    onClick = { viewModel.onIntent(WebNovelDetailIntent.ContinueReading) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    Text("이어읽기")
                }
                OutlinedButton(
                    onClick = { viewModel.onIntent(WebNovelDetailIntent.PrepareOffline) },
                    enabled = !state.preparing,
                ) {
                    Text(if (state.preparing) "준비 중…" else "↓ 오프라인 준비")
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "챕터 ${item.totalChapters}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    TextButton(onClick = { viewModel.onIntent(WebNovelDetailIntent.CheckForNewChapters) }) {
                        Text("↻ 새 화 확인")
                    }
                }
            }
        }

        items(state.chapters, key = { it.index }) { chapter ->
            ChapterRow(chapter)
            HorizontalDivider()
        }
    }
}

private fun metaLine(item: LibraryItem): String {
    val serial =
        when (item.serialStatus) {
            SerialStatus.ONGOING -> "연재중"
            SerialStatus.COMPLETED -> "완결"
            SerialStatus.UNKNOWN -> "-"
        }
    return "${item.author} · ${item.siteName} · $serial · 전 ${item.totalChapters}화"
}

@Composable
private fun ChapterRow(chapter: Chapter) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = chapter.title, style = MaterialTheme.typography.bodyLarge)
        TranslationBadge(chapter.translationStatus)
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
    AssistChip(onClick = {}, enabled = false, label = { Text(label) })
}
