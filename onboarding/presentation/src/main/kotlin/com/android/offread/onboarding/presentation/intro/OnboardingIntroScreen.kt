package com.android.offread.onboarding.presentation.intro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.offread.core.domain.navigation.HomePage
import com.android.offread.core.ui.helper.LocalMessageHelper
import com.android.offread.core.ui.helper.LocalNavigationHelper
import com.android.offread.core.ui.helper.singleClickable

/**
 * O-02 온보딩 인트로·언어쌍 선택(F-002).
 */
@Composable
fun OnboardingIntroScreen(
    modifier: Modifier = Modifier,
    viewModel: OnboardingIntroViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navigationHelper = LocalNavigationHelper.current
    val messageHelper = LocalMessageHelper.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                OnboardingIntroEffect.NavigateNext -> navigationHelper.navigateTo(HomePage)
                is OnboardingIntroEffect.ShowError -> messageHelper.showToast(effect.message)
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
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "✈  →  📖", style = MaterialTheme.typography.headlineSmall)
        }

        Text(
            text = "인터넷 없이,\n이름은 고정된 채로.",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "기기 안에서 번역하는 나만의 원서 리더 —\n읽을 언어를 선택해 주세요.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.options.forEach { option ->
                LanguagePairRow(
                    option = option,
                    selected = option.pair in state.selected,
                    onClick = { viewModel.onIntent(OnboardingIntroIntent.TogglePair(option.pair)) },
                )
            }
        }

        Button(
            onClick = { viewModel.onIntent(OnboardingIntroIntent.Proceed) },
            enabled = state.canProceed,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(52.dp),
        ) {
            Text(text = "다음")
        }
    }
}

@Composable
private fun LanguagePairRow(
    option: LanguagePairOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor =
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val contentColor =
        if (option.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .singleClickable(enabled = option.enabled, onClick = onClick)
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = selected, onClick = onClick, enabled = option.enabled)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = option.pair.displayLabel(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
            )
            Text(
                text = option.pair.displaySubtitle(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
