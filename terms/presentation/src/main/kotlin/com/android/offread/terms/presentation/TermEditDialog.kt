package com.android.offread.terms.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
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
 * T-02 용어 편집 다이얼로그(F-026). 추가·수정 공용. 수정일 때만 삭제 노출.
 */
@Composable
internal fun TermEditDialog(
    dialog: TermDialog?,
    onDismiss: () -> Unit,
    onSubmit: (source: String, translation: String, pinned: Boolean) -> Unit,
    onDelete: (id: String) -> Unit,
) {
    if (dialog == null) return
    val editing = (dialog as? TermDialog.Edit)?.term

    var source by remember(dialog) { mutableStateOf(editing?.source.orEmpty()) }
    var translation by remember(dialog) { mutableStateOf(editing?.translation.orEmpty()) }
    var pinned by remember(dialog) { mutableStateOf(editing?.pinned ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editing == null) "용어 추가" else "용어 편집") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    singleLine = true,
                    label = { Text("원어") },
                )
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
                    Switch(checked = pinned, onCheckedChange = { pinned = it })
                    Column {
                        Text("항상 이 번역 사용", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "번역 시 강제 적용 (고정)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(source, translation, pinned) },
                enabled = source.isNotBlank() && translation.isNotBlank(),
            ) { Text("저장") }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (editing != null) {
                    TextButton(onClick = { onDelete(editing.id) }) { Text("삭제") }
                }
                TextButton(onClick = onDismiss) { Text("취소") }
            }
        },
    )
}
