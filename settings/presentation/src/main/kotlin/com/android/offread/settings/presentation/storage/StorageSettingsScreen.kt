package com.android.offread.settings.presentation.storage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
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
import com.android.offread.core.ui.helper.LocalMessageHelper
import com.android.offread.core.ui.helper.LocalNavigationHelper
import com.android.offread.settings.domain.ModelSettingsPage
import com.android.offread.settings.domain.model.DownloadedContent
import com.android.offread.settings.presentation.models.formatSize

/**
 * S-05 저장·캐시 관리(F-031). 캐시 용량·콘텐츠 목록과 비우기, 모델 관리로 가는 링크.
 */
@Composable
fun StorageSettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: StorageSettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val messageHelper = LocalMessageHelper.current
    val navigationHelper = LocalNavigationHelper.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is StorageSettingsEffect.ShowMessage -> messageHelper.showToast(effect.message)
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(24.dp)) {
        Text(text = "저장·캐시", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        ElevatedCard(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "번역 캐시", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "${formatSize(state.cache.bytes)} · 문단 ${state.cache.entryCount}개",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "캐시를 비우면 저장한 화를 다시 번역해야 해요. 원문과 모델은 그대로 남아요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.onIntent(StorageSettingsIntent.ClearAllClicked) },
                        enabled = state.cache.entryCount > 0,
                    ) {
                        Text("캐시 비우기")
                    }
                    TextButton(onClick = { navigationHelper.navigateTo(ModelSettingsPage) }) { Text("모델 관리") }
                }
            }
        }

        Text(
            text = "저장된 콘텐츠",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 24.dp),
        )

        if (state.isEmpty) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = "아직 저장된 번역이 없어요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.contents, key = { it.item.id }) { content ->
                    ContentRow(
                        content = content,
                        onClear = { viewModel.onIntent(StorageSettingsIntent.ClearItemClicked(content)) },
                    )
                }
            }
        }
    }

    state.clearTarget?.let { target ->
        val title =
            when (target) {
                ClearTarget.All -> "번역 캐시를 비울까요?"
                is ClearTarget.Item -> "${target.content.item.title} 의 캐시를 비울까요?"
            }
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(StorageSettingsIntent.DismissClear) },
            title = { Text(title) },
            text = { Text("비우면 되돌릴 수 없고, 다음에 읽을 때 다시 번역해요.") },
            confirmButton = {
                TextButton(onClick = { viewModel.onIntent(StorageSettingsIntent.ConfirmClear) }) { Text("비우기") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(StorageSettingsIntent.DismissClear) }) { Text("취소") }
            },
        )
    }
}

@Composable
private fun ContentRow(
    content: DownloadedContent,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = content.item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${formatSize(content.bytes)} · 문단 ${content.cachedSegments}개",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedButton(onClick = onClear) { Text("비우기") }
        }
    }
}
