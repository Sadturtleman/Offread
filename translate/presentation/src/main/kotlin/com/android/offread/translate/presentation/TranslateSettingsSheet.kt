package com.android.offread.translate.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.offread.translate.domain.model.TranslationEngineKind

/**
 * 설정 시트: 엔진 선택, TranslateGemma 모델 파일 관리, 번역 캐시 비우기.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TranslateSettingsSheet(
    state: TranslateUiState,
    onDismiss: () -> Unit,
    onSelectEngine: (TranslationEngineKind) -> Unit,
    onImportModel: (String) -> Unit,
    onDeleteModel: (String) -> Unit,
    onClearCache: () -> Unit,
) {
    val picker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { onImportModel(it.toString()) }
        }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "번역 엔진", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TranslationEngineKind.entries.forEach { kind ->
                    FilterChip(
                        selected = state.engine == kind,
                        onClick = { onSelectEngine(kind) },
                        label = { Text(kind.label()) },
                    )
                }
            }
            Text(
                text = state.engine.description(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (state.engine.requiresModelFile) {
                Text(
                    text = "모델 파일",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp),
                )
                if (state.models.isEmpty()) {
                    Text(
                        text = "가져온 모델이 없어요. 내려받은 .litertlm 파일을 골라 주세요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    state.models.forEach { file ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = file.name, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = formatSize(file.sizeBytes),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            TextButton(onClick = { onDeleteModel(file.name) }) { Text("삭제") }
                        }
                    }
                }
                OutlinedButton(onClick = { picker.launch(arrayOf("*/*")) }, enabled = !state.importing) {
                    Text(if (state.importing) "가져오는 중…" else "모델 파일 가져오기")
                }
            }

            Text(
                text = "번역 캐시",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = "${formatSize(state.cache.bytes)} · 문단 ${state.cache.entryCount}개",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = onClearCache, enabled = state.cache.entryCount > 0) {
                Text("캐시 비우기")
            }
        }
    }
}

internal fun TranslationEngineKind.label(): String =
    when (this) {
        TranslationEngineKind.ML_KIT -> "ML Kit"
        TranslationEngineKind.TRANSLATE_GEMMA -> "TranslateGemma 4B"
    }

private fun TranslationEngineKind.description(): String =
    when (this) {
        TranslationEngineKind.ML_KIT ->
            "언어쌍당 약 30MB 모델을 자동으로 받아 바로 번역해요. 가볍고 빠르지만 품질은 전용 모델보다 낮아요."
        TranslationEngineKind.TRANSLATE_GEMMA ->
            "번역 전용 모델이라 품질이 가장 좋아요. INT4 기준 약 2GB · RAM 6GB 이상 권장이고, " +
                "모델 파일을 직접 가져와야 해요. 첫 번역은 모델을 올리느라 10초쯤 걸려요."
    }

internal fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "0MB"
    val gb = bytes.toDouble() / (1024 * 1024 * 1024)
    if (gb >= 1.0) return "%.1fGB".format(gb)
    val mb = bytes.toDouble() / (1024 * 1024)
    if (mb >= 1.0) return "%.0fMB".format(mb)
    return "%.0fKB".format(bytes.toDouble() / 1024)
}
