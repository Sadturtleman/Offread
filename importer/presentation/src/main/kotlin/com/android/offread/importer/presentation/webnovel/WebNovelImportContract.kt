package com.android.offread.importer.presentation.webnovel

import com.android.offread.core.ui.mvi.MviEffect
import com.android.offread.core.ui.mvi.MviIntent
import com.android.offread.core.ui.mvi.ReducerEvent
import com.android.offread.core.ui.mvi.UiState
import com.android.offread.importer.domain.model.WebNovelMetadata
import com.android.offread.library.domain.model.Collection

data class WebNovelImportUiState(
    val url: String = "",
    val recognizing: Boolean = false,
    val metadata: WebNovelMetadata? = null,
    val collections: List<Collection> = emptyList(),
    val selectedCollectionId: String? = null,
    val importing: Boolean = false,
) : UiState {
    val canRecognize: Boolean get() = url.isNotBlank() && !recognizing
    val canImport: Boolean get() = metadata != null && selectedCollectionId != null && !importing
}

sealed interface WebNovelImportIntent : MviIntent {
    data class UrlChanged(
        val url: String,
    ) : WebNovelImportIntent

    data object Recognize : WebNovelImportIntent

    data class SelectCollection(
        val id: String,
    ) : WebNovelImportIntent

    data class CreateCollection(
        val name: String,
    ) : WebNovelImportIntent

    data object Import : WebNovelImportIntent
}

sealed interface WebNovelImportEvent : ReducerEvent {
    data class UrlChanged(
        val url: String,
    ) : WebNovelImportEvent

    data class Recognizing(
        val recognizing: Boolean,
    ) : WebNovelImportEvent

    data class Recognized(
        val metadata: WebNovelMetadata,
    ) : WebNovelImportEvent

    data class CollectionsChanged(
        val collections: List<Collection>,
    ) : WebNovelImportEvent

    data class CollectionSelected(
        val id: String,
    ) : WebNovelImportEvent

    data class Importing(
        val importing: Boolean,
    ) : WebNovelImportEvent
}

sealed interface WebNovelImportEffect : MviEffect {
    data class ShowError(
        val message: String,
    ) : WebNovelImportEffect

    /** 가져오기 성공 → 이전 화면(라이브러리)으로. */
    data object Done : WebNovelImportEffect
}
