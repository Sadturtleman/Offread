package com.android.offread.translate.domain.usecase

import com.android.offread.core.entity.LanguagePair
import com.android.offread.translate.domain.TermCandidate
import com.android.offread.translate.domain.TermCandidateExtractor
import com.android.offread.translate.domain.TermSuggestionSink
import com.android.offread.translate.domain.TranslationEngine
import javax.inject.Inject

/**
 * F-024: 번역한 원문에서 용어 후보를 뽑아 캐논 번역값과 함께 컬렉션 용어맵에 제안한다.
 *
 * 후보의 번역값은 용어 단독으로 한 번 더 추론해 얻는다 — 문장 번역 결과에서 대응 부분을
 * 정렬(alignment)로 찾는 것보다 단순하고, 캐논값으로 바로 쓸 수 있는 형태가 나온다.
 * 후보 하나가 실패해도 나머지 제안은 계속한다.
 */
class SuggestTermsUseCase
    @Inject
    constructor(
        private val extractor: TermCandidateExtractor,
        private val engine: TranslationEngine,
        private val sink: TermSuggestionSink,
    ) {
        suspend operator fun invoke(
            collectionId: String,
            languagePair: LanguagePair,
            originals: List<String>,
        ): List<TermCandidate> {
            val known = sink.existingSources(collectionId)
            val candidates = extractor.extract(originals, known)
            return candidates.filter { candidate ->
                val translation =
                    runCatching { engine.translate(candidate.source, languagePair, emptyList()) }
                        .getOrNull()
                        ?.trim()
                if (translation.isNullOrEmpty()) {
                    false
                } else {
                    sink.suggest(collectionId, candidate.source, translation, candidate.occurrenceCount)
                    true
                }
            }
        }
    }
