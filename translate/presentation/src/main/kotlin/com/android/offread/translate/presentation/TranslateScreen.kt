package com.android.offread.translate.presentation

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

/**
 * 유일한 화면. 주소를 넣으면 그 페이지를 웹뷰에 그대로 띄우고, 일본어만 한국어로 바꿔 끼운다.
 */
@Composable
fun TranslateScreen(
    modifier: Modifier = Modifier,
    viewModel: TranslateViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val messageHelper = LocalMessageHelper.current
    var webView by remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TranslateEffect.ShowMessage -> messageHelper.showToast(effect.message)
                is TranslateEffect.ApplyTranslation -> webView?.applyTranslation(effect.id, effect.text)
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 20.dp).padding(top = 16.dp)) {
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
                modifier = Modifier.fillMaxWidth(),
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
                Text(if (state.loading) "여는 중…" else "번역해서 보기")
            }

            if (state.modelMissing) {
                ModelDownloadCard(
                    state = state,
                    onDownload = { viewModel.onIntent(TranslateIntent.DownloadModel) },
                    onCancel = { viewModel.onIntent(TranslateIntent.CancelDownload) },
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            TranslationStatus(
                state = state,
                onRetry = { viewModel.onIntent(TranslateIntent.RetryFailed) },
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        val url = state.loadedUrl
        if (url == null) {
            Hint(
                text = "일본어 웹페이지 주소를 넣으면 그 페이지를 그대로 띄우고 글자만 한국어로 바꿔요.",
                modifier = Modifier.weight(1f),
            )
        } else {
            TranslateWebView(
                url = url,
                onPageLoad = { viewModel.onIntent(TranslateIntent.PageLoaded) },
                onCollectTexts = { viewModel.onIntent(TranslateIntent.TextsCollected(it)) },
                onWebViewReady = { webView = it },
                modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 12.dp),
            )
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

/** 몇 개나 바뀌었는지. 번역은 위에서부터 하나씩 들어가므로 진행 중에도 읽을 수 있다. */
@Composable
private fun TranslationStatus(
    state: TranslateUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.total == 0) return
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text =
                    if (state.translating) {
                        "${state.translated + state.failed} / ${state.total} 문단 번역 중…"
                    } else {
                        "${state.translated}개 문단을 번역했어요" + if (state.failed > 0) " · ${state.failed}개 실패" else ""
                    },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!state.translating && state.failed > 0) {
                TextButton(onClick = onRetry) { Text("실패한 문단 다시") }
            }
        }
        if (state.translating) {
            LinearProgressIndicator(progress = { state.progress }, modifier = Modifier.fillMaxWidth())
        }
    }
}

/**
 * 모델이 아직 없을 때 뜨는 카드. Wi-Fi 면 ViewModel 이 이미 받기 시작했고, 종량제 망이면
 * 여기 버튼이 시작점이다. 2GB 라 크기를 먼저 보여 준다.
 */
@Composable
private fun ModelDownloadCard(
    state: TranslateUiState,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val download = state.download
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "번역 모델이 필요해요",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        if (download == null) {
            Text(
                text = "TranslateGemma 4B · ${formatSize(state.modelSizeBytes)}. 한 번 받으면 오프라인에서도 번역해요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onDownload) { Text("모델 내려받기") }
            return@Column
        }
        Text(
            text = "${formatSize(download.downloadedBytes)} / ${formatSize(download.totalBytes)} 받는 중…",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LinearProgressIndicator(progress = { download.fraction }, modifier = Modifier.fillMaxWidth())
        OutlinedButton(onClick = onCancel) { Text("멈추기") }
    }
}

@Composable
private fun Hint(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
