package com.android.offread.importer.presentation.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.offread.core.ui.helper.LocalNavigationHelper
import com.android.offread.core.ui.helper.singleClickable
import com.android.offread.importer.domain.WebNovelImportPage

/**
 * I-01 가져오기 유형 선택(F-011). 웹소설 URL / 논문 PDF(준비중).
 */
@Composable
fun ImportSheetScreen(modifier: Modifier = Modifier) {
    val navigationHelper = LocalNavigationHelper.current

    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "무엇을 가져올까요?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        ImportOption(
            title = "웹소설 URL로 가져오기",
            subtitle = "지원 사이트의 작품 주소 붙여넣기",
            enabled = true,
            onClick = { navigationHelper.navigateTo(WebNovelImportPage) },
        )
        ImportOption(
            title = "논문 PDF 가져오기",
            subtitle = "준비 중 · Phase 2",
            enabled = false,
            onClick = {},
        )
    }
}

@Composable
private fun ImportOption(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val alpha = if (enabled) 1f else 0.5f
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f * alpha))
                .singleClickable(enabled = enabled, onClick = onClick)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
        )
    }
}
