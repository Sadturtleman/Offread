package com.android.offread.translate.domain

import com.android.offread.core.entity.LanguagePair
import javax.inject.Inject

/**
 * TranslateGemma 프롬프트(F-020). 이 모델은 지시문이 아니라 **태그 형식**으로 학습돼 있다:
 * `<src>ja</src><dst>ko</dst><text>원문</text>` (ISO 639-1).
 *
 * 형식이 고정이라 용어 지시문을 끼울 자리가 없다 — 용어맵은 파이프라인 후처리에서
 * 고정 용어 치환으로 보장한다(GlossaryPostProcessor).
 */
class TranslateGemmaPromptBuilder
    @Inject
    constructor() {
        fun build(
            text: String,
            pair: LanguagePair,
        ): String = "<src>${pair.source.code}</src><dst>${pair.target.code}</dst><text>$text</text>"

        /** 모델이 태그를 되풀이하거나 따옴표를 붙이는 경우를 걷어낸다. */
        fun clean(raw: String): String {
            val withoutTags =
                raw
                    .replace(TAG_REGEX, "")
                    .trim()
            return withoutTags
                .removeSurrounding("\"")
                .removeSurrounding("「", "」")
                .trim()
        }

        private companion object {
            val TAG_REGEX = Regex("</?(?:src|dst|text)>")
        }
    }
