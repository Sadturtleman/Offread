package com.android.offread.onboarding.presentation.firsttranslation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.offread.core.domain.navigation.HomePage
import com.android.offread.core.ui.helper.LocalNavigationHelper

/**
 * O-04 첫 번역 체험(F-004). 샘플 원문 → 온디바이스 번역 시연 → 라이브러리.
 */
@Composable
fun FirstTranslationScreen(
    modifier: Modifier = Modifier,
    viewModel: FirstTranslationViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navigationHelper = LocalNavigationHelper.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                FirstTranslationEffect.NavigateToLibrary -> navigationHelper.navigateTo(HomePage)
            }
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "첫 번역을 경험해 보세요",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "인터넷 연결 없이, 지금 이 기기 안에서.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        val sample = state.sample
        if (sample != null) {
            TextCard(
                label = "원문",
                body = sample.originalText,
            )

            if (state.translated == null) {
                OutlinedButton(
                    onClick = { viewModel.onIntent(FirstTranslationIntent.Translate) },
                    enabled = state.canTranslate,
                ) {
                    Text(text = "↓ 번역하기")
                }
            }

            state.translated?.let { translated ->
                TextCard(
                    label = "번역 · 기기 안에서 완료 ✓",
                    body = translated,
                    highlighted = true,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { viewModel.onIntent(FirstTranslationIntent.Start) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(52.dp),
        ) {
            Text(text = "오프리드 시작하기")
        }
    }
}

@Composable
private fun TextCard(
    label: String,
    body: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
) {
    val container =
        if (highlighted) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        }
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(container)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
