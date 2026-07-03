package com.android.offread.importer.presentation.webnovel

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.offread.core.ui.helper.LocalMessageHelper
import com.android.offread.core.ui.helper.LocalNavigationHelper

/**
 * I-02 웹소설 URL 가져오기(F-012).
 */
@Composable
fun WebNovelImportScreen(
    modifier: Modifier = Modifier,
    viewModel: WebNovelImportViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navigationHelper = LocalNavigationHelper.current
    val messageHelper = LocalMessageHelper.current
    var newCollectionName by remember { mutableStateOf("") }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                WebNovelImportEffect.Done -> {
                    messageHelper.showToast("가져왔어요.")
                    navigationHelper.navigateToBack()
                }
                is WebNovelImportEffect.ShowError -> messageHelper.showToast(effect.message)
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "웹소설 가져오기", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Text(text = "작품 URL", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = state.url,
            onValueChange = { viewModel.onIntent(WebNovelImportIntent.UrlChanged(it)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("https://ncode.syosetu.com/...") },
        )
        Text(
            text = "지원 사이트: 소설가가 되자 · 카쿠요무",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedButton(
            onClick = { viewModel.onIntent(WebNovelImportIntent.Recognize) },
            enabled = state.canRecognize,
        ) {
            Text(if (state.recognizing) "인식 중…" else "URL 인식")
        }

        state.metadata?.let { meta ->
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("${meta.siteName} ✓", style = MaterialTheme.typography.labelMedium)
                Text(meta.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    "${meta.author} · 전 ${meta.totalChapters}화",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Text(text = "저장할 컬렉션", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.collections.forEach { collection ->
                FilterChip(
                    selected = collection.id == state.selectedCollectionId,
                    onClick = { viewModel.onIntent(WebNovelImportIntent.SelectCollection(collection.id)) },
                    label = { Text(collection.name) },
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = newCollectionName,
                onValueChange = { newCollectionName = it },
                singleLine = true,
                modifier = Modifier.weight(1f),
                placeholder = { Text("새 컬렉션 이름") },
            )
            AssistChip(
                onClick = {
                    viewModel.onIntent(WebNovelImportIntent.CreateCollection(newCollectionName))
                    newCollectionName = ""
                },
                label = { Text("+ 새 컬렉션") },
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { viewModel.onIntent(WebNovelImportIntent.Import) },
            enabled = state.canImport,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text("가져오기")
        }
    }
}
