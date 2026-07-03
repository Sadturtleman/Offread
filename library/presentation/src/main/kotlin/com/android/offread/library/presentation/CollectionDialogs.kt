package com.android.offread.library.presentation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * 컬렉션 생성/이름변경/삭제 다이얼로그(F-006). 삭제는 연쇄 삭제를 경고한다(P-04, 휴지통 없음).
 */
@Composable
internal fun CollectionDialogs(
    dialog: CollectionDialog?,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onConfirmDelete: (String) -> Unit,
) {
    when (dialog) {
        null -> Unit

        CollectionDialog.Create ->
            NameInputDialog(
                title = "새 컬렉션",
                initial = "",
                confirmLabel = "만들기",
                onDismiss = onDismiss,
                onConfirm = onCreate,
            )

        is CollectionDialog.Rename ->
            NameInputDialog(
                title = "이름 변경",
                initial = dialog.collection.name,
                confirmLabel = "저장",
                onDismiss = onDismiss,
                onConfirm = { onRename(dialog.collection.id, it) },
            )

        is CollectionDialog.ConfirmDelete ->
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("컬렉션 삭제") },
                text = {
                    Text(
                        "‘${dialog.collection.name}’과(와) 하위 컬렉션·아이템·용어맵·캐시가 모두 삭제돼요. " +
                            "되돌릴 수 없어요.",
                    )
                },
                confirmButton = {
                    TextButton(onClick = { onConfirmDelete(dialog.collection.id) }) { Text("삭제") }
                },
                dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
            )
    }
}

@Composable
private fun NameInputDialog(
    title: String,
    initial: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf(initial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text("이름") },
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank(),
            ) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
