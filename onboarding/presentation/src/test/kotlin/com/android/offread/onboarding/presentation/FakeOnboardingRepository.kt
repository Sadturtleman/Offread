package com.android.offread.onboarding.presentation

import com.android.offread.core.entity.LanguagePair
import com.android.offread.onboarding.domain.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** 인메모리 [OnboardingRepository] 테스트 더블(presentation 단위 테스트용). */
class FakeOnboardingRepository(
    initialComplete: Boolean = false,
    initialPairs: Set<LanguagePair> = emptySet(),
) : OnboardingRepository {
    private val complete = MutableStateFlow(initialComplete)
    private val pairs = MutableStateFlow(initialPairs)

    override val isOnboardingComplete = complete.asStateFlow()
    override val selectedLanguagePairs = pairs.asStateFlow()

    override suspend fun setSelectedLanguagePairs(pairs: Set<LanguagePair>) {
        this.pairs.value = pairs
    }

    override suspend fun setOnboardingComplete(complete: Boolean) {
        this.complete.value = complete
    }
}
