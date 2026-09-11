package com.android.offread.translate.domain.usecase

import com.android.offread.core.entity.LanguagePair
import com.android.offread.translate.domain.SegmentCache
import com.android.offread.translate.domain.SegmentSplitter
import com.android.offread.translate.domain.TranslationEngine
import com.android.offread.translate.domain.TranslationEngineUnavailableException
import com.android.offread.translate.domain.model.SegmentCacheKey
import com.android.offread.translate.domain.model.TranslatedText
import com.android.offread.translate.domain.model.VisibleText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * 웹뷰가 긁어 온 텍스트 노드들을 번역한다(#58).
 *
 * 화면이 하나씩 받아 그 자리에 채우도록 [Flow] 로 흘려보낸다 — 긴 페이지에서 다 끝날 때까지
 * 기다리지 않아도 위에서부터 한국어로 바뀐다.
 *
 * 노드 하나가 길면 모델 컨텍스트(1024 토큰)를 넘기므로 문장 경계에서 쪼개 번역하고 다시 잇는다.
 * 캐시는 조각 단위라, 같은 문장이 다른 페이지에 또 나오면 추론을 건너뛴다(F-021).
 */
class TranslateTextsUseCase
    @Inject
    constructor(
        private val splitter: SegmentSplitter,
        private val engine: TranslationEngine,
        private val cache: SegmentCache,
    ) {
        operator fun invoke(
            texts: List<VisibleText>,
            pair: LanguagePair,
        ): Flow<TranslatedText> =
            flow {
                val modelVersion = engine.modelVersion(pair)
                for (text in texts) {
                    emit(translate(text, pair, modelVersion))
                }
            }

        private suspend fun translate(
            text: VisibleText,
            pair: LanguagePair,
            modelVersion: String,
        ): TranslatedText {
            val translated = mutableListOf<String>()
            for (segment in splitter.split(text.text)) {
                val key = SegmentCacheKey.of(segment.original, modelVersion)
                val cached = cache.get(key)
                if (cached != null) {
                    translated += cached
                    continue
                }
                val result =
                    runCatching { engine.translate(segment.original, pair) }
                        .getOrElse { error ->
                            if (error is TranslationEngineUnavailableException) throw error
                            return TranslatedText(text.id, translated = null)
                        }
                cache.put(key, result)
                translated += result
            }
            return TranslatedText(text.id, translated.joinToString(" "))
        }
    }
