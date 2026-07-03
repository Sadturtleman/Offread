package com.android.offread.onboarding.domain.usecase

import com.android.offread.core.entity.TranslationModel
import com.android.offread.onboarding.domain.TranslationModelRepository
import com.android.offread.onboarding.domain.model.ModelDownloadStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** 모델 다운로드를 대기열에 넣는다(F-003). */
class EnqueueModelDownloadsUseCase
    @Inject
    constructor(
        private val translationModelRepository: TranslationModelRepository,
    ) {
        suspend operator fun invoke(models: List<TranslationModel>) = translationModelRepository.enqueue(models)
    }

/** 모델별 다운로드 상태 스트림. */
class ObserveModelDownloadsUseCase
    @Inject
    constructor(
        private val translationModelRepository: TranslationModelRepository,
    ) {
        operator fun invoke(): Flow<Map<String, ModelDownloadStatus>> = translationModelRepository.observeDownloads()
    }

/** 특정 모델 다운로드 일시정지. */
class PauseModelDownloadUseCase
    @Inject
    constructor(
        private val translationModelRepository: TranslationModelRepository,
    ) {
        suspend operator fun invoke(modelId: String) = translationModelRepository.pause(modelId)
    }

/** 일시정지한 모델 다운로드 재개. */
class ResumeModelDownloadUseCase
    @Inject
    constructor(
        private val translationModelRepository: TranslationModelRepository,
    ) {
        suspend operator fun invoke(modelId: String) = translationModelRepository.resume(modelId)
    }
