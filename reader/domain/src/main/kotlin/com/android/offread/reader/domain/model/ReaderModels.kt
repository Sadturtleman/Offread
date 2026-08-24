package com.android.offread.reader.domain.model

import com.android.offread.core.entity.TranslationStatus

/**
 * 리더 본문의 최소 단위(문장/문단). 미번역이면 원문을 노출하고 인라인 재시도를 제공한다(P-08).
 *
 * @property translated 번역문(없으면 미번역 → 원문 표시)
 */
data class ReaderSegment(
    val id: String,
    val original: String,
    val translated: String?,
) {
    val isTranslated: Boolean get() = translated != null

    /** 화면에 보여줄 텍스트: 번역문이 있으면 번역문, 없으면 원문. */
    val displayText: String get() = translated ?: original
}

/**
 * 리더가 표시하는 한 챕터의 콘텐츠(F-015).
 */
data class ChapterContent(
    val itemId: String,
    val chapterIndex: Int,
    val title: String,
    val segments: List<ReaderSegment>,
) {
    /** 이 챕터의 배지 상태(F-019 공통 규칙). 세그먼트가 전부 번역돼야 오프라인 열람 가능으로 본다. */
    val translationStatus: TranslationStatus
        get() = TranslationStatus.of(total = segments.size, translated = segments.count { it.isTranslated })
}

/** 리더 테마(F-016). */
enum class ReaderTheme {
    LIGHT,
    DARK,
    SEPIA,
}

/**
 * 리더 표시 설정(F-016 공통 셸). 지금은 인메모리이며, 추후 표시 설정(S-03)과 동기화한다.
 */
data class ReaderSettings(
    val fontScale: Float = 1.0f,
    val theme: ReaderTheme = ReaderTheme.LIGHT,
) {
    companion object {
        const val MIN_FONT_SCALE = 0.8f
        const val MAX_FONT_SCALE = 1.6f
    }
}
