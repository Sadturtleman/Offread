package com.android.offread.translate.domain

import com.android.offread.translate.domain.model.TranslationEngineKind
import kotlinx.coroutines.flow.Flow

/**
 * 선택된 번역 엔진 저장 포트(F-020). 기본값은 [TranslationEngineKind.ML_KIT] —
 * 별도 모델 파일 없이 바로 번역이 되므로 첫 실행에서 막히지 않는다.
 */
interface TranslationEnginePreference {
    val selected: Flow<TranslationEngineKind>

    suspend fun select(kind: TranslationEngineKind)
}
