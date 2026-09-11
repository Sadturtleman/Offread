package com.android.offread.translate.domain

import com.android.offread.core.entity.LanguagePair
import com.android.offread.translate.domain.model.WebPage

/**
 * 온디바이스 추론 포트. 구현은 ML Kit / TranslateGemma(LiteRT-LM) 어댑터.
 */
interface TranslationEngine {
    suspend fun translate(
        text: String,
        pair: LanguagePair,
    ): String

    /** 현재 모델 버전(캐시 키 구성요소). 엔진·모델이 바뀌면 값이 달라진다. */
    suspend fun modelVersion(pair: LanguagePair): String
}

/**
 * 엔진을 쓸 준비가 안 됐을 때(예: TranslateGemma 모델 파일 없음).
 *
 * 세그먼트 하나의 번역 실패와 달리 페이지 전체가 번역될 수 없는 상황이라, 유스케이스가
 * 삼키지 않고 화면까지 올려 사용자가 이유를 보게 한다.
 */
class TranslationEngineUnavailableException(
    message: String,
) : IllegalStateException(message)

/**
 * 웹페이지 수집 포트. HTTP·HTML 파싱은 어댑터에 감춘다.
 */
interface WebPageSource {
    suspend fun fetch(url: String): WebPage
}
