package com.android.offread.translate.domain

import com.android.offread.translate.domain.model.TranslationEngineKind
import kotlinx.coroutines.flow.Flow

/**
 * 선택된 번역 엔진 저장 포트(F-020). 기본값은 [TranslationEngineKind.TRANSLATE_GEMMA] —
 * 번역 품질이 제품의 존재 이유라, 모델 파일을 한 번 가져오는 수고를 감수한다.
 */
interface TranslationEnginePreference {
    val selected: Flow<TranslationEngineKind>

    suspend fun select(kind: TranslationEngineKind)
}
