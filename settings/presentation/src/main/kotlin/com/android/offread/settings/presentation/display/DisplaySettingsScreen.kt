package com.android.offread.settings.presentation.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.offread.core.ui.helper.LocalMessageHelper
import com.android.offread.settings.domain.model.DisplaySettings
import com.android.offread.settings.domain.model.DisplayTheme
import com.android.offread.settings.domain.model.TranslationDisplayMode
import kotlin.math.roundToInt

/**
 * S-03 표시 설정(F-030). 폰트 크기·테마·번역 표시 방식을 미리보기와 함께 조정한다.
 */
@Composable
fun DisplaySettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: DisplaySettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val messageHelper = LocalMessageHelper.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DisplaySettingsEffect.ShowError -> messageHelper.showToast(effect.message)
            }
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
    ) {
        Text(text = "표시 설정", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        SectionTitle("폰트 크기")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Slider(
                value = state.settings.fontScale,
                onValueChange = { viewModel.onIntent(DisplaySettingsIntent.ChangeFontScale(it)) },
                valueRange = DisplaySettings.MIN_FONT_SCALE..DisplaySettings.MAX_FONT_SCALE,
                steps = 7,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${(state.settings.fontScale * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        SectionTitle("테마")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DisplayTheme.entries.forEach { theme ->
                FilterChip(
                    selected = state.settings.theme == theme,
                    onClick = { viewModel.onIntent(DisplaySettingsIntent.ChangeTheme(theme)) },
                    label = { Text(theme.label()) },
                )
            }
        }

        SectionTitle("기본 번역 표시")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TranslationDisplayMode.entries.forEach { mode ->
                FilterChip(
                    selected = state.settings.translationDisplay == mode,
                    onClick = { viewModel.onIntent(DisplaySettingsIntent.ChangeTranslationDisplay(mode)) },
                    label = { Text(mode.label()) },
                )
            }
        }

        SectionTitle("미리보기")
        PreviewCard(settings = state.settings)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
    )
}

@Composable
private fun PreviewCard(
    settings: DisplaySettings,
    modifier: Modifier = Modifier,
) {
    val (background, textColor) =
        when (settings.theme) {
            DisplayTheme.LIGHT -> Color(0xFFFFFFFF) to Color(0xFF1A1A1A)
            DisplayTheme.DARK -> Color(0xFF121212) to Color(0xFFE6E6E6)
            DisplayTheme.SEPIA -> Color(0xFFF4ECD8) to Color(0xFF5B4636)
        }
    val bodyStyle = MaterialTheme.typography.bodyLarge
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = background, shape = MaterialTheme.shapes.medium)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (settings.translationDisplay == TranslationDisplayMode.BILINGUAL) {
            Text(
                text = "彼女は静かに本を閉じた。",
                style = bodyStyle,
                fontSize = bodyStyle.fontSize * settings.fontScale,
                color = textColor.copy(alpha = 0.6f),
            )
        }
        Text(
            text = "그녀는 조용히 책을 덮었다.",
            style = bodyStyle,
            fontSize = bodyStyle.fontSize * settings.fontScale,
            color = textColor,
        )
    }
}

private fun DisplayTheme.label(): String =
    when (this) {
        DisplayTheme.LIGHT -> "라이트"
        DisplayTheme.DARK -> "다크"
        DisplayTheme.SEPIA -> "세피아"
    }

private fun TranslationDisplayMode.label(): String =
    when (this) {
        TranslationDisplayMode.TRANSLATED_ONLY -> "번역만"
        TranslationDisplayMode.BILINGUAL -> "원문·번역 병행"
    }
