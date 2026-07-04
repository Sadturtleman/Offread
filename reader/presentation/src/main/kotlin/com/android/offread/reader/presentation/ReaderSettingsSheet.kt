package com.android.offread.reader.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.offread.reader.domain.model.ReaderSettings
import com.android.offread.reader.domain.model.ReaderTheme

/**
 * F-016 리더 설정 시트 — 폰트 크기·테마(라이트/다크/세피아).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReaderSettingsSheet(
    settings: ReaderSettings,
    onFontScale: (Float) -> Unit,
    onTheme: (ReaderTheme) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "표시 설정", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            Text(text = "글자 크기", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = settings.fontScale,
                onValueChange = onFontScale,
                valueRange = ReaderSettings.MIN_FONT_SCALE..ReaderSettings.MAX_FONT_SCALE,
            )

            Text(text = "테마", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReaderTheme.entries.forEach { theme ->
                    FilterChip(
                        selected = settings.theme == theme,
                        onClick = { onTheme(theme) },
                        label = { Text(theme.label()) },
                    )
                }
            }
        }
    }
}

private fun ReaderTheme.label(): String =
    when (this) {
        ReaderTheme.LIGHT -> "라이트"
        ReaderTheme.DARK -> "다크"
        ReaderTheme.SEPIA -> "세피아"
    }
