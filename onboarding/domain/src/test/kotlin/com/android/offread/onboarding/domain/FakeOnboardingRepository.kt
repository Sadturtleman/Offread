package com.android.offread.onboarding.domain

import com.android.offread.core.entity.LanguagePair
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** 인메모리 [OnboardingRepository] 테스트 더블. */
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
