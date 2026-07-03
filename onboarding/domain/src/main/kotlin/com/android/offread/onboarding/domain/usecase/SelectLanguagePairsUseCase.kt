package com.android.offread.onboarding.domain.usecase

import com.android.offread.core.entity.LanguagePair
import com.android.offread.onboarding.domain.OnboardingRepository
import javax.inject.Inject

/**
 * F-002 언어쌍 선택 저장. 최소 1개 이상, 선택 가능한(AVAILABLE) 언어쌍만 허용한다.
 */
class SelectLanguagePairsUseCase
    @Inject
    constructor(
        private val onboardingRepository: OnboardingRepository,
    ) {
        suspend operator fun invoke(pairs: Set<LanguagePair>): Result<Unit> {
            if (pairs.isEmpty()) {
                return Result.failure(NoLanguagePairSelectedException)
            }
            val unavailable = pairs.filterNot { it.isSelectable }.toSet()
            if (unavailable.isNotEmpty()) {
                return Result.failure(UnavailableLanguagePairException(unavailable))
            }
            onboardingRepository.setSelectedLanguagePairs(pairs)
            return Result.success(Unit)
        }
    }

/** 언어쌍을 하나도 고르지 않았을 때. */
object NoLanguagePairSelectedException : IllegalArgumentException("최소 한 개의 언어쌍을 선택해야 합니다.")

/** 아직 제공하지 않는(COMING_SOON) 언어쌍을 골랐을 때. */
class UnavailableLanguagePairException(
    val pairs: Set<LanguagePair>,
) : IllegalArgumentException("선택할 수 없는 언어쌍: $pairs")
