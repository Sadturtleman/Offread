package com.android.offread.translate.domain

import javax.inject.Inject

/**
 * 자동 추출된 용어 후보(F-024).
 *
 * @property occurrenceCount 이번 번역 범위에서의 출현 수(용어맵 목록의 '출현 수')
 */
data class TermCandidate(
    val source: String,
    val occurrenceCount: Int,
)

/**
 * F-024 1단계: 원문에서 고유명사·반복 용어 후보를 뽑는다.
 *
 * 규칙은 일본어 웹소설(MVP 주 대상)에 맞춰 **카타카나 연속 표기**를 고유명사 신호로 쓴다.
 * 인명·지명·고유 개념이 대부분 카타카나로 적히고, 이들이 화마다 일관되게 번역돼야 하기 때문이다.
 * 중국어 원문의 후보 추출은 형태소 분석이 필요해 후속 범위다.
 *
 * 첫 세션 제안 수락률이 활성화 KPI 이므로 과다 제안을 피한다 —
 * [minOccurrences] 회 이상 나온 후보만, [limit] 개까지만 제안한다.
 */
class TermCandidateExtractor
    @Inject
    constructor() {
        fun extract(
            originals: List<String>,
            known: Set<String> = emptySet(),
            minOccurrences: Int = DEFAULT_MIN_OCCURRENCES,
            limit: Int = DEFAULT_LIMIT,
        ): List<TermCandidate> {
            val counts = mutableMapOf<String, Int>()
            originals.forEach { text ->
                KATAKANA_RUN.findAll(text).forEach { match ->
                    val candidate = match.value.trim('ー', '・')
                    if (candidate.length >= MIN_LENGTH && candidate !in known) {
                        counts[candidate] = (counts[candidate] ?: 0) + 1
                    }
                }
            }
            return counts
                .asSequence()
                .filter { it.value >= minOccurrences }
                .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenByDescending { it.key.length })
                .take(limit)
                .map { TermCandidate(source = it.key, occurrenceCount = it.value) }
                .toList()
        }

        /**
         * F-017: 본문 롱프레스 시 고를 수 있는 표기들. 자동 제안(반복 2회 이상)과 달리
         * 한 번만 나온 표기도 사용자가 직접 고르는 대상이므로 전부 돌려준다.
         * CJK 는 띄어쓰기가 없어 카타카나·한자 연속 표기를 단어 후보로 본다.
         */
        fun wordsIn(text: String): List<String> =
            WORD_RUN
                .findAll(text)
                .map { it.value.trim('ー', '・') }
                .filter { it.length >= MIN_LENGTH }
                .distinct()
                .toList()

        private companion object {
            /** 카타카나(장음·중점 포함) 연속 표기. */
            val KATAKANA_RUN = Regex("[ァ-ヶー・]{2,}")
            const val MIN_LENGTH = 2
            const val DEFAULT_MIN_OCCURRENCES = 2
            const val DEFAULT_LIMIT = 5

            /** 카타카나 런 또는 한자 런 — CJK 원문에서 고유명사가 될 만한 덩어리. */
            val WORD_RUN = Regex("[ァ-ヶー・]{2,}|[一-龯]{2,}")
        }
    }
