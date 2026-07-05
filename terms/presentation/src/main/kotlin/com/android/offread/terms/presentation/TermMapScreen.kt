package com.android.offread.terms.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import com.android.offread.terms.domain.model.Term
import com.android.offread.terms.domain.model.TermFilter
import com.android.offread.terms.domain.model.TermOrigin
import com.android.offread.terms.domain.model.TermStatus

/**
 * T-01 용어맵(F-025) + T-02 용어 편집(F-026).
 */
@Composable
fun TermMapScreen(
    collectionId: String,
    modifier: Modifier = Modifier,
    viewModel: TermMapViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val messageHelper = LocalMessageHelper.current

    LaunchedEffect(collectionId) { viewModel.start(collectionId) }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TermMapEffect.ShowError -> messageHelper.showToast(effect.message)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Text(text = "용어맵", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TermFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = state.filter == filter,
                        onClick = { viewModel.onIntent(TermMapIntent.ChangeFilter(filter)) },
                        label = { Text(filter.label()) },
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.terms, key = { it.id }) { term ->
                    TermRow(
                        term = term,
                        onEdit = { viewModel.onIntent(TermMapIntent.EditClicked(term)) },
                        onAccept = { viewModel.onIntent(TermMapIntent.AcceptSuggestion(term)) },
                        onReject = { viewModel.onIntent(TermMapIntent.RejectSuggestion(term)) },
                    )
                }
            }

            Button(
                onClick = { viewModel.onIntent(TermMapIntent.AddClicked) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("+ 용어 추가")
            }
        }
    }

    TermEditDialog(
        dialog = state.dialog,
        onDismiss = { viewModel.onIntent(TermMapIntent.DismissDialog) },
        onSubmit = { source, translation, pinned ->
            viewModel.onIntent(TermMapIntent.Submit(source, translation, pinned))
        },
        onDelete = { viewModel.onIntent(TermMapIntent.Delete(it)) },
    )
}

@Composable
private fun TermRow(
    term: Term,
    onEdit: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${term.source} → ${term.translation}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = term.subtitle(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (term.status == TermStatus.SUGGESTED) {
                TextButton(onClick = onAccept) { Text("수락") }
                TextButton(onClick = onReject) { Text("거절") }
            } else {
                TextButton(onClick = onEdit) { Text("편집") }
            }
        }
    }
}

private fun TermFilter.label(): String =
    when (this) {
        TermFilter.ALL -> "전체"
        TermFilter.AUTO -> "자동"
        TermFilter.MANUAL -> "수동"
        TermFilter.PINNED -> "고정"
    }

private fun Term.subtitle(): String {
    if (status == TermStatus.SUGGESTED) return "자동 제안 — 확인해 주세요"
    val originLabel = if (origin == TermOrigin.AUTO) "자동" else "수동"
    val detail =
        when {
            pinned -> "고정"
            origin == TermOrigin.AUTO -> "${occurrenceCount}회 등장"
            else -> ""
        }
    return if (detail.isEmpty()) originLabel else "$originLabel · $detail"
}
