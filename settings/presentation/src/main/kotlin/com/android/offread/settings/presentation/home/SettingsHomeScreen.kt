package com.android.offread.settings.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.offread.core.ui.helper.LocalMessageHelper
import com.android.offread.core.ui.helper.LocalNavigationHelper
import com.android.offread.core.ui.helper.singleClickable
import com.android.offread.settings.domain.DisplaySettingsPage
import com.android.offread.settings.domain.InfoPage

private const val PREPARING_MESSAGE = "준비 중이에요."

/**
 * S-01 설정 홈(F-028). 미구현 항목은 준비중 안내만 노출한다.
 */
@Composable
fun SettingsHomeScreen(modifier: Modifier = Modifier) {
    val navigationHelper = LocalNavigationHelper.current
    val messageHelper = LocalMessageHelper.current

    Column(modifier = modifier.fillMaxSize().padding(24.dp)) {
        Text(text = "설정", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SettingsMenuRow(
                title = "번역 엔진·모델",
                description = "엔진 선택, 모델 다운로드 관리",
                onClick = { messageHelper.showToast(PREPARING_MESSAGE) },
            )
            SettingsMenuRow(
                title = "표시",
                description = "폰트 크기, 테마, 번역 표시 방식",
                onClick = { navigationHelper.navigateTo(DisplaySettingsPage) },
            )
            SettingsMenuRow(
                title = "용어맵 전역 설정",
                description = "자동 제안, 기본 적용 범위",
                onClick = { messageHelper.showToast(PREPARING_MESSAGE) },
            )
            SettingsMenuRow(
                title = "저장·캐시",
                description = "번역 캐시 용량, 정리",
                onClick = { messageHelper.showToast(PREPARING_MESSAGE) },
            )
            SettingsMenuRow(
                title = "정보",
                description = "앱 버전, 약관, 오픈소스 라이선스",
                onClick = { navigationHelper.navigateTo(InfoPage) },
            )
        }
    }
}

@Composable
private fun SettingsMenuRow(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().singleClickable(onClick = onClick).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
