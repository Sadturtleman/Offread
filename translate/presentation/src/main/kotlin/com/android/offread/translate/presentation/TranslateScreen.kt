package com.android.offread.translate.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.offread.core.ui.helper.LocalMessageHelper
import com.android.offread.translate.domain.model.TranslatedSegment

/**
 * 유일한 화면. URL 을 넣으면 그 페이지의 일본어를 한국어로 번역해 보여준다.
 */
@Composable
fun TranslateScreen(
    modifier: Modifier = Modifier,
    viewModel: TranslateViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val messageHelper = LocalMessageHelper.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TranslateEffect.ShowMessage -> messageHelper.showToast(effect.message)
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Offread", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            TextButton(onClick = { viewModel.onIntent(TranslateIntent.OpenSettings) }) { Text("설정") }
        }

        OutlinedTextField(
            value = state.url,
            onValueChange = { viewModel.onIntent(TranslateIntent.UrlChanged(it)) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            singleLine = true,
            label = { Text("웹페이지 주소") },
            placeholder = { Text("https://…") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { viewModel.onIntent(TranslateIntent.Translate) }),
        )

        Button(
            onClick = { viewModel.onIntent(TranslateIntent.Translate) },
            enabled = state.canTranslate,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            Text(if (state.loading) "번역 중…" else "번역하기")
        }

        when {
            state.loading && state.page == null ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            state.page == null ->
                Hint(
                    text = "일본어 웹페이지 주소를 넣으면 기기 안에서 번역해요.",
                    modifier = Modifier.weight(1f),
                )
            else -> {
                val page = state.page ?: return@Column
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp),
                )
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(page.segments, key = { it.id }) { segment ->
                        SegmentView(
                            segment = segment,
                            retrying = state.retryingSegmentId == segment.id,
                            onRetry = { viewModel.onIntent(TranslateIntent.RetrySegment(segment.id)) },
                        )
                    }
                }
            }
        }
    }

    if (state.settingsVisible) {
        TranslateSettingsSheet(
            state = state,
            onDismiss = { viewModel.onIntent(TranslateIntent.CloseSettings) },
            onSelectEngine = { viewModel.onIntent(TranslateIntent.SelectEngine(it)) },
            onImportModel = { viewModel.onIntent(TranslateIntent.ImportModel(it)) },
            onDeleteModel = { viewModel.onIntent(TranslateIntent.DeleteModel(it)) },
            onClearCache = { viewModel.onIntent(TranslateIntent.ClearCache) },
        )
    }
}

@Composable
private fun SegmentView(
    segment: TranslatedSegment,
    retrying: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (segment.translated != null) {
        Text(
            text = segment.translated.orEmpty(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = modifier,
        )
        return
    }
    // 실패한 문단은 원문을 보여주고 그 자리에서 다시 시도한다 — 페이지 전체를 막지 않는다.
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = segment.original,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onRetry, enabled = !retrying) {
            Text(if (retrying) "다시 번역 중…" else "다시 번역")
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
