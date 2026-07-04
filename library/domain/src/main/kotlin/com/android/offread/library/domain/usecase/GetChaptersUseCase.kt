package com.android.offread.library.domain.usecase

import com.android.offread.library.domain.model.Chapter
import com.android.offread.library.domain.model.LibraryItem
import javax.inject.Inject

/**
 * F-008: 아이템의 화수로부터 챕터 목록을 만든다.
 *
 * NOTE: 실제 챕터 제목·화별 상태는 스크래핑/리더에서 채운다. 현재는 화 번호 기반 합성이며
 * 모든 화의 번역 상태는 아이템의 상태를 따른다.
 */
class GetChaptersUseCase
    @Inject
    constructor() {
        operator fun invoke(item: LibraryItem): List<Chapter> =
            (1..item.totalChapters).map { index ->
                Chapter(
                    index = index,
                    title = "${index}화",
                    translationStatus = item.translationStatus,
                )
            }
    }
