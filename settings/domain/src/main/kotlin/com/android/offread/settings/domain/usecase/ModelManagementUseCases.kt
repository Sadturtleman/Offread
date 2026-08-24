package com.android.offread.settings.domain.usecase

import com.android.offread.core.entity.LanguagePair
import com.android.offread.settings.domain.model.ManagedModel
import com.android.offread.translate.domain.TranslationModelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * F-029: 선택 가능한 언어쌍의 모델을 설치 여부·진행 상태와 함께 보여준다.
 * 제공 예정(COMING_SOON) 언어쌍은 카탈로그에 없으므로 목록에도 나오지 않는다.
 */
class ObserveManagedModelsUseCase
    @Inject
    constructor(
        private val translationModelRepository: TranslationModelRepository,
    ) {
        operator fun invoke(): Flow<List<ManagedModel>> {
            val catalog = translationModelRepository.catalogFor(LanguagePair.entries.toSet())
            return combine(
                translationModelRepository.installedLanguagePairs,
                translationModelRepository.observeDownloads(),
            ) { installed, downloads ->
                catalog.map { model ->
                    ManagedModel(
                        model = model,
                        installed = model.languagePair in installed,
                        status = downloads[model.id],
                    )
                }
            }
        }
    }

/** F-029: 언어쌍별 모델 다운로드. */
class DownloadModelUseCase
    @Inject
    constructor(
        private val translationModelRepository: TranslationModelRepository,
    ) {
        suspend operator fun invoke(model: com.android.offread.core.entity.TranslationModel) =
            translationModelRepository.enqueue(listOf(model))
    }

/** F-029: 설치된 모델 삭제(용량 회수). */
class DeleteModelUseCase
    @Inject
    constructor(
        private val translationModelRepository: TranslationModelRepository,
    ) {
        suspend operator fun invoke(modelId: String) = translationModelRepository.delete(modelId)
    }
