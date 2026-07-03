package com.android.offread.onboarding.domain

import com.android.offread.core.entity.LanguagePair
import com.android.offread.core.entity.TranslationModel
import com.android.offread.onboarding.domain.model.ModelDownloadStatus
import kotlinx.coroutines.flow.Flow

/**
 * 번역 모델 카탈로그·다운로드·설치 상태 포트(헥사고날). WorkManager/HTTP/체크섬 같은 무거운 인프라는
 * 이 포트 뒤 어댑터(onboarding:data)에 감춘다.
 */
interface TranslationModelRepository {
    /** 주어진 언어쌍들에 대응하는 모델 카탈로그(제공 예정 언어쌍은 제외될 수 있음). */
    fun catalogFor(pairs: Set<LanguagePair>): List<TranslationModel>

    /** 설치 완료된 모델의 언어쌍 집합. */
    val installedLanguagePairs: Flow<Set<LanguagePair>>

    /** 모델 id → 현재 다운로드 상태 스트림. */
    fun observeDownloads(): Flow<Map<String, ModelDownloadStatus>>

    suspend fun enqueue(models: List<TranslationModel>)

    suspend fun pause(modelId: String)

    suspend fun resume(modelId: String)
}
