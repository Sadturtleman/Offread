package com.android.offread.onboarding.presentation.download

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.android.offread.onboarding.domain.FirstTranslationPage

/**
 * O-03 번역 모델 다운로드(F-003). 진행 상태·일시정지·나중에 하기.
 */
@Composable
fun ModelDownloadScreen(
    modifier: Modifier = Modifier,
    viewModel: ModelDownloadViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navigationHelper = LocalNavigationHelper.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ModelDownloadEffect.NavigateToFirstTranslation -> navigationHelper.navigateTo(FirstTranslationPage)
                ModelDownloadEffect.NavigateToLibrary -> navigationHelper.navigateTo(HomePage)
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
            text = "번역 모델을 준비하고 있어요",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "다운로드 ${state.completedCount}/${state.total}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        state.items.forEach { item ->
            ModelDownloadCard(
                item = item,
                onTogglePause = { viewModel.onIntent(ModelDownloadIntent.TogglePause(item.model.id)) },
            )
        }

        InfoBanner()

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = { viewModel.onIntent(ModelDownloadIntent.SkipForNow) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "나중에 하기")
        }
    }
}

@Composable
private fun ModelDownloadCard(
    item: ModelDownloadItem,
    onTogglePause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = item.model.displayName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
        LinearProgressIndicator(
            progress = { item.status.fractionOrZero() },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(6.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = item.status.progressLine(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!item.isCompleted) {
                OutlinedButton(onClick = onTogglePause) {
                    Text(text = if (item.isPaused) "재개" else "일시정지")
                }
            }
        }
    }
}

@Composable
private fun InfoBanner(modifier: Modifier = Modifier) {
    Text(
        text = "Wi-Fi에서 받는 것을 권장해요. 백그라운드에서도 계속돼요.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                .padding(16.dp),
    )
}
