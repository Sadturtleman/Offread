package com.android.offread.translate.domain

import com.android.offread.translate.domain.model.GlossaryEntry
import javax.inject.Inject

/**
 * F-020 후처리 / F-026 '고정' 용어 강제 적용.
 *
 * 프롬프트로 용어를 주입해도 모델이 무시할 수 있으므로, 고정 용어는 번역문에 원어가 그대로
 * 남아 있으면 캐논 번역값으로 치환한다. 고정이 아닌 용어는 모델 판단을 존중해 건드리지 않는다.
 */
class GlossaryPostProcessor
    @Inject
    constructor() {
        fun apply(
            translation: String,
            glossary: List<GlossaryEntry>,
        ): String =
            glossary
                .filter { it.pinned && it.source.isNotBlank() }
                // 긴 원어부터 치환해야 짧은 용어가 긴 용어의 일부를 먼저 먹지 않는다.
                .sortedByDescending { it.source.length }
                .fold(translation) { text, entry -> text.replace(entry.source, entry.translation) }
    }
