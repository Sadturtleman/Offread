package com.android.offread.reader.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.offread.core.ui.helper.LocalMessageHelper
import com.android.offread.reader.domain.model.ReaderSegment

/**
 * R-01 웹소설 리더(F-015) + 리더 설정 셸(F-016).
 */
@Composable
fun ReaderScreen(
    itemId: String,
    chapterIndex: Int,
    modifier: Modifier = Modifier,
    viewModel: ReaderViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val messageHelper = LocalMessageHelper.current

    LaunchedEffect(itemId, chapterIndex) { viewModel.start(itemId, chapterIndex) }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ReaderEffect.ShowError -> messageHelper.showToast(effect.message)
            }
        }
    }

    val content = state.content
    Column(modifier = modifier.fillMaxSize()) {
        // 상단 헤더
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.itemTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = content?.title ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            TextButton(onClick = { viewModel.onIntent(ReaderIntent.OpenSettings) }) { Text("Aa") }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(content?.segments.orEmpty(), key = { it.id }) { segment ->
                SegmentView(
                    segment = segment,
                    fontScale = state.settings.fontScale,
                    onRetry = { viewModel.onIntent(ReaderIntent.RetrySegment(segment.id)) },
                )
            }
        }

        // 하단 내비게이션
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = { viewModel.onIntent(ReaderIntent.PreviousChapter) }, enabled = state.hasPrevious) {
                Text("← 이전 화")
            }
            Text(
                text = if (state.totalChapters > 0) "${state.chapterIndex} / ${state.totalChapters}" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = { viewModel.onIntent(ReaderIntent.NextChapter) }, enabled = state.hasNext) {
                Text("다음 화 →")
            }
        }
    }

    if (state.settingsVisible) {
        ReaderSettingsSheet(
            settings = state.settings,
            onFontScale = { viewModel.onIntent(ReaderIntent.ChangeFontScale(it)) },
            onTheme = { viewModel.onIntent(ReaderIntent.ChangeTheme(it)) },
            onDismiss = { viewModel.onIntent(ReaderIntent.CloseSettings) },
        )
    }
}

@Composable
private fun SegmentView(
    segment: ReaderSegment,
    fontScale: Float,
    onRetry: () -> Unit,
) {
    if (segment.isTranslated) {
        Text(
            text = segment.displayText,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp * fontScale),
        )
    } else {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = segment.original,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp * fontScale),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "이 문장은 아직 번역되지 않았어요",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onRetry) { Text("↻ 재시도") }
            }
        }
    }
}
