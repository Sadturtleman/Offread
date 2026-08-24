package com.android.offread.core.ui.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.offread.core.entity.TranslationStatus

/**
 * F-019 상태 배지. 라이브러리·상세·리더가 모두 이 컴포저블 하나를 쓴다.
 * 라벨·색 규칙을 여기서만 정의해 화면마다 어긋나지 않게 한다.
 */
@Composable
fun TranslationStatusBadge(
    status: TranslationStatus,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val container: Color
    val content: Color
    when (status) {
        TranslationStatus.UNTRANSLATED -> {
            container = colors.surfaceVariant
            content = colors.onSurfaceVariant
        }
        TranslationStatus.TRANSLATING -> {
            container = colors.secondaryContainer
            content = colors.onSecondaryContainer
        }
        TranslationStatus.CACHED -> {
            container = colors.primaryContainer
            content = colors.onPrimaryContainer
        }
        TranslationStatus.CLOUD_FALLBACK -> {
            container = colors.tertiaryContainer
            content = colors.onTertiaryContainer
        }
    }

    Text(
        text = status.badgeLabel(),
        style = MaterialTheme.typography.labelSmall,
        color = content,
        modifier =
            modifier
                .clip(RoundedCornerShape(BADGE_CORNER))
                .background(container)
                .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

/** 배지 라벨(F-019 4종). 텍스트로만 필요한 곳에서도 같은 문구를 쓰도록 공개한다. */
fun TranslationStatus.badgeLabel(): String =
    when (this) {
        TranslationStatus.UNTRANSLATED -> "미번역"
        TranslationStatus.TRANSLATING -> "번역 중"
        TranslationStatus.CACHED -> "오프라인 가능"
        TranslationStatus.CLOUD_FALLBACK -> "클라우드 폴백"
    }

private val BADGE_CORNER = 8.dp
