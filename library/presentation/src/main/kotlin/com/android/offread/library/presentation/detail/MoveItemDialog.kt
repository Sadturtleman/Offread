package com.android.offread.library.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.offread.core.ui.helper.singleClickable
import com.android.offread.library.domain.model.Collection
import com.android.offread.library.domain.model.TermMapMoveStrategy

/**
 * F-007 컬렉션 이동 다이얼로그: 대상 컬렉션 선택 + 용어맵 처리(함께 이동/병합/남겨두기).
 */
@Composable
internal fun MoveItemDialog(
    visible: Boolean,
    currentCollectionId: String,
    collections: List<Collection>,
    onDismiss: () -> Unit,
    onSubmit: (targetCollectionId: String, strategy: TermMapMoveStrategy) -> Unit,
) {
    if (!visible) return
    val candidates = collections.filterNot { it.id == currentCollectionId }
    var targetId by remember(candidates.size) { mutableStateOf(candidates.firstOrNull()?.id) }
    var strategy by remember { mutableStateOf(TermMapMoveStrategy.MOVE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("컬렉션 이동") },
        text = {
            if (candidates.isEmpty()) {
                Text("이동할 다른 컬렉션이 없어요. 먼저 컬렉션을 만들어 주세요.")
            } else {
                Column(
                    modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(text = "대상 컬렉션", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    candidates.forEach { collection ->
                        ChoiceRow(
                            label = collection.name,
                            selected = targetId == collection.id,
                            onClick = { targetId = collection.id },
                        )
                    }

                    Text(
                        text = "용어맵 처리",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    TermMapMoveStrategy.entries.forEach { candidate ->
                        ChoiceRow(
                            label = candidate.label(),
                            description = candidate.description(),
                            selected = strategy == candidate,
                            onClick = { strategy = candidate },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { targetId?.let { onSubmit(it, strategy) } },
                enabled = targetId != null,
            ) {
                Text("이동")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        },
    )
}

@Composable
private fun ChoiceRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().singleClickable(onClick = onClick).padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Column(modifier = Modifier.padding(start = 4.dp)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun TermMapMoveStrategy.label(): String =
    when (this) {
        TermMapMoveStrategy.MOVE -> "함께 이동"
        TermMapMoveStrategy.MERGE -> "병합"
        TermMapMoveStrategy.LEAVE -> "남겨두기"
    }

private fun TermMapMoveStrategy.description(): String =
    when (this) {
        TermMapMoveStrategy.MOVE -> "원 컬렉션 용어를 대상 컬렉션으로 옮겨요."
        TermMapMoveStrategy.MERGE -> "용어를 대상 컬렉션에 복사하고 원본은 남겨요."
        TermMapMoveStrategy.LEAVE -> "용어맵은 그대로 둬요."
    }
