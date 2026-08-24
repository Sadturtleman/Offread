package com.android.offread.translate.domain

import com.android.offread.core.entity.Language
import com.android.offread.core.entity.LanguagePair
import com.android.offread.translate.domain.model.GlossaryEntry
import javax.inject.Inject

/**
 * F-020: LLM 엔진에 넣을 번역 프롬프트를 만든다.
 *
 * 용어맵을 프롬프트에 넣어 캐논 번역을 먼저 유도하고(후처리 치환은 그래도 최후 방어선이다),
 * 모델이 설명·따옴표를 덧붙이지 않도록 "번역문만" 을 명시한다. 순수 함수라 도메인에 둔다.
 */
class TranslationPromptBuilder
    @Inject
    constructor() {
        fun build(
            text: String,
            pair: LanguagePair,
            glossary: List<GlossaryEntry>,
        ): String {
            val source = pair.source.displayName()
            val target = pair.target.displayName()
            val builder = StringBuilder()
            builder.append("다음 $source 문장을 $target(으)로 번역해라.\n")
            builder.append("번역문만 출력하고 설명·원문·따옴표를 덧붙이지 마라.\n")
            val terms = glossary.filter { it.source.isNotBlank() && it.translation.isNotBlank() }
            if (terms.isNotEmpty()) {
                builder.append("아래 용어는 반드시 지정된 번역을 써라.\n")
                // 고정 용어를 먼저 보여 준다 — 프롬프트 앞쪽이 더 잘 지켜진다.
                terms.sortedByDescending { it.pinned }.forEach { entry ->
                    builder.append("- ${entry.source} → ${entry.translation}\n")
                }
            }
            builder.append("\n$source 원문:\n")
            builder.append(text)
            builder.append("\n\n$target 번역문:\n")
            return builder.toString()
        }

        /** 모델 응답에서 군더더기를 걷어낸다. */
        fun clean(raw: String): String =
            raw
                .trim()
                .removePrefix("번역문:")
                .removePrefix("번역:")
                .trim()
                .removeSurrounding("\"")
                .removeSurrounding("「", "」")
                .trim()
    }

private fun Language.displayName(): String =
    when (this) {
        Language.KOREAN -> "한국어"
        Language.JAPANESE -> "일본어"
        Language.CHINESE -> "중국어"
        Language.ENGLISH -> "영어"
    }
