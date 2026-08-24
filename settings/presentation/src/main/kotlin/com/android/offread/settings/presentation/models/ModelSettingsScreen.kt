package com.android.offread.settings.presentation.models

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
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
import com.android.offread.core.entity.Language
import com.android.offread.core.entity.LanguagePair
import com.android.offread.core.ui.helper.LocalMessageHelper
import com.android.offread.settings.domain.model.ManagedModel
import com.android.offread.translate.domain.model.ModelDownloadStatus
import com.android.offread.translate.domain.model.TranslationEngineKind

/**
 * S-02 엔진·모델 관리(F-029). 설치 모델 목록·용량과 언어쌍별 다운로드/삭제.
 */
@Composable
fun ModelSettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: ModelSettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val messageHelper = LocalMessageHelper.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ModelSettingsEffect.ShowMessage -> messageHelper.showToast(effect.message)
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(24.dp)) {
        Text(text = "번역 엔진·모델", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            text = "설치됨 ${formatSize(state.installedBytes)} · 기기 안에서만 번역해요",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )

        EngineSection(
            selected = state.engine,
            onSelect = { viewModel.onIntent(ModelSettingsIntent.SelectEngine(it)) },
        )

        Text(
            text = "언어쌍별 모델",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 24.dp),
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.models, key = { it.model.id }) { managed ->
                ModelRow(
                    managed = managed,
                    onDownload = { viewModel.onIntent(ModelSettingsIntent.Download(managed)) },
                    onDelete = { viewModel.onIntent(ModelSettingsIntent.DeleteClicked(managed)) },
                )
            }
        }
    }

    state.deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(ModelSettingsIntent.DismissDelete) },
            title = { Text("${target.model.displayName} 을 지울까요?") },
            text = {
                Text(
                    "${target.model.languagePair.label()} 번역을 하려면 ${formatSize(target.model.sizeBytes)} 를 다시 " +
                        "내려받아야 해요. 저장한 번역 캐시는 그대로 남아요.",
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onIntent(ModelSettingsIntent.ConfirmDelete) }) { Text("삭제") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(ModelSettingsIntent.DismissDelete) }) { Text("취소") }
            },
        )
    }
}

@Composable
private fun EngineSection(
    selected: TranslationEngineKind,
    onSelect: (TranslationEngineKind) -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(modifier = modifier.fillMaxWidth().padding(top = 16.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "번역 엔진", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TranslationEngineKind.entries.forEach { kind ->
                    FilterChip(
                        selected = selected == kind,
                        onClick = { onSelect(kind) },
                        label = { Text(kind.label()) },
                    )
                }
            }
            Text(
                text = selected.description(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

internal fun TranslationEngineKind.label(): String =
    when (this) {
        TranslationEngineKind.ML_KIT -> "ML Kit"
        TranslationEngineKind.ON_DEVICE_LLM -> "온디바이스 LLM"
    }

private fun TranslationEngineKind.description(): String =
    when (this) {
        TranslationEngineKind.ML_KIT ->
            "언어쌍당 약 30MB 모델을 자동으로 받아 바로 번역해요. 가볍지만 용어맵은 번역 후 치환으로만 반영돼요."
        TranslationEngineKind.ON_DEVICE_LLM ->
            "앱 전용 저장소 llm 폴더의 .task 또는 .litertlm 모델을 써요. 용어맵을 프롬프트로 넣어 " +
                "일관성이 높지만 모델 파일을 직접 넣어야 해요."
    }

@Composable
private fun ModelRow(
    managed: ManagedModel,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "${managed.model.languagePair.label()} · ${managed.model.displayName}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${formatSize(managed.model.sizeBytes)} · ${managed.statusLine()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                when {
                    managed.installed -> OutlinedButton(onClick = onDelete) { Text("삭제") }
                    managed.isBusy -> Unit
                    else -> OutlinedButton(onClick = onDownload) { Text("내려받기") }
                }
            }

            (managed.status as? ModelDownloadStatus.Downloading)?.let { downloading ->
                LinearProgressIndicator(progress = { downloading.fraction }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

private fun ManagedModel.statusLine(): String {
    if (installed) return "설치됨"
    return when (val current = status) {
        null -> "설치 안 됨"
        ModelDownloadStatus.Queued -> "대기 중"
        is ModelDownloadStatus.Downloading -> "${(current.fraction * 100).toInt()}% 내려받는 중"
        ModelDownloadStatus.Paused -> "일시정지됨"
        is ModelDownloadStatus.Failed -> "실패 · 재시도 ${current.attempt}회"
        ModelDownloadStatus.Completed -> "설치됨"
    }
}

internal fun LanguagePair.label(): String = "${source.short()}→${target.short()}"

private fun Language.short(): String =
    when (this) {
        Language.KOREAN -> "한"
        Language.JAPANESE -> "일"
        Language.CHINESE -> "중"
        Language.ENGLISH -> "영"
    }

internal fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "0MB"
    val gb = bytes.toDouble() / (1024 * 1024 * 1024)
    if (gb >= 1.0) return "%.1fGB".format(gb)
    return "%.0fMB".format(bytes.toDouble() / (1024 * 1024))
}
