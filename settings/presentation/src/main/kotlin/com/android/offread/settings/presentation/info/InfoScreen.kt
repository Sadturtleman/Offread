package com.android.offread.settings.presentation.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.offread.core.ui.helper.LocalMessageHelper
import com.android.offread.core.ui.helper.singleClickable

private const val PREPARING_MESSAGE = "준비 중이에요."

/**
 * S-06 정보(F-032). 앱 버전·약관·라이선스·문의와 '내 데이터는 어디에?' 고지(P-01).
 */
@Composable
fun InfoScreen(
    modifier: Modifier = Modifier,
    viewModel: InfoViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val messageHelper = LocalMessageHelper.current

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "정보",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        InfoValueRow(title = "앱 버전", value = state.appInfo?.versionName.orEmpty())
        InfoLinkRow(title = "이용약관", onClick = { messageHelper.showToast(PREPARING_MESSAGE) })
        InfoLinkRow(title = "개인정보처리방침", onClick = { messageHelper.showToast(PREPARING_MESSAGE) })
        InfoLinkRow(title = "오픈소스 라이선스", onClick = { messageHelper.showToast(PREPARING_MESSAGE) })
        InfoValueRow(title = "문의", value = state.appInfo?.contactEmail.orEmpty())

        DataNoticeCard(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun InfoValueRow(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InfoLinkRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().singleClickable(onClick = onClick).padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun DataNoticeCard(modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "내 데이터는 어디에?",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "원문·번역 결과·용어맵·독서 기록은 모두 이 기기 안에만 저장돼요. 서버로 전송하지 않아요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
