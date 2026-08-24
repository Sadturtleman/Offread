package com.android.offread.reader.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * F-017 용어 빠른편집 다이얼로그들. 리더 안에서 T-02 와 같은 항목(원어·번역·고정)을 다루되,
 * 저장 뒤 '현재 챕터 재번역' 확인을 덧붙인다.
 */
@Composable
internal fun TermQuickEditDialogs(
    quickEdit: TermQuickEdit?,
    onDismiss: () -> Unit,
    onPickWord: (String) -> Unit,
    onSubmit: (translation: String, pinned: Boolean) -> Unit,
    onConfirmRetranslate: (Boolean) -> Unit,
) {
    when (quickEdit) {
        null -> Unit
        is TermQuickEdit.PickWord ->
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("용어로 등록할 표기") },
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        quickEdit.words.forEach { word ->
                            AssistChip(onClick = { onPickWord(word) }, label = { Text(word) })
                        }
                    }
                },
                confirmButton = { TextButton(onClick = onDismiss) { Text("닫기") } },
            )
        is TermQuickEdit.Edit -> EditDialog(quickEdit.source, onDismiss, onSubmit)
        TermQuickEdit.ConfirmRetranslate ->
            AlertDialog(
                onDismissRequest = { onConfirmRetranslate(false) },
                title = { Text("현재 화를 다시 번역할까요?") },
                text = { Text("새 용어를 반영하려면 이 화의 번역 캐시를 지우고 다시 번역해요.") },
                confirmButton = { TextButton(onClick = { onConfirmRetranslate(true) }) { Text("다시 번역") } },
                dismissButton = { TextButton(onClick = { onConfirmRetranslate(false) }) { Text("나중에") } },
            )
    }
}

@Composable
private fun EditDialog(
    source: String,
    onDismiss: () -> Unit,
    onSubmit: (translation: String, pinned: Boolean) -> Unit,
) {
    var translation by remember(source) { mutableStateOf("") }
    var pinned by remember(source) { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("용어 편집") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = source, style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = translation,
                    onValueChange = { translation = it },
                    singleLine = true,
                    label = { Text("번역") },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "고정", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = pinned, onCheckedChange = { pinned = it })
                    Text(
                        text = "번역 시 강제 적용",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSubmit(translation, pinned) }) { Text("저장") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
