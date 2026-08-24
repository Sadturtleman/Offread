package com.android.offread.translate.domain

import com.android.offread.translate.domain.model.Segment
import javax.inject.Inject

/**
 * F-020 1단계: 원문을 번역 단위로 쪼갠다.
 *
 * 문단(빈 줄) 을 기본 단위로 삼되, 한 문단이 [maxChars] 를 넘으면 문장 경계에서 더 자른다.
 * 세그먼트가 지나치게 길면 추론 지연이 커지고(성능 목표 문단당 p95 < 1.5s), 지나치게 짧으면
 * 문맥이 끊겨 번역 품질이 떨어지므로 문단을 기본으로 둔다.
 */
class SegmentSplitter
    @Inject
    constructor() {
        fun split(
            text: String,
            maxChars: Int = DEFAULT_MAX_CHARS,
        ): List<Segment> =
            text
                .split(PARAGRAPH_DELIMITER)
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .flatMap { paragraph -> splitParagraph(paragraph, maxChars) }
                .mapIndexed { index, chunk -> Segment(id = "seg-${index + 1}", original = chunk) }

        private fun splitParagraph(
            paragraph: String,
            maxChars: Int,
        ): List<String> {
            if (paragraph.length <= maxChars) return listOf(paragraph)
            val chunks = mutableListOf<String>()
            val builder = StringBuilder()
            for (sentence in paragraph.splitSentences()) {
                if (builder.isNotEmpty() && builder.length + sentence.length > maxChars) {
                    chunks += builder.toString()
                    builder.clear()
                }
                builder.append(sentence)
            }
            if (builder.isNotEmpty()) chunks += builder.toString()
            return chunks
        }

        /** 문장부호 뒤에서 끊되 부호는 앞 문장에 남긴다(일본어·중국어 부호 포함). */
        private fun String.splitSentences(): List<String> {
            val sentences = mutableListOf<String>()
            val builder = StringBuilder()
            for (char in this) {
                builder.append(char)
                if (char in SENTENCE_ENDINGS) {
                    sentences += builder.toString()
                    builder.clear()
                }
            }
            if (builder.isNotEmpty()) sentences += builder.toString()
            return sentences
        }

        private companion object {
            const val DEFAULT_MAX_CHARS = 400
            val PARAGRAPH_DELIMITER = Regex("\\n\\s*\\n")
            val SENTENCE_ENDINGS = setOf('。', '．', '.', '!', '！', '?', '？')
        }
    }
