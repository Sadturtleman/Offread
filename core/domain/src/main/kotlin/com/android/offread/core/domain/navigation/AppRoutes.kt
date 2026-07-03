package com.android.offread.core.domain.navigation

/**
 * 앱의 모든 화면 path 단일 소스(single source of truth).
 *
 * feature 의 Page 객체는 path 문자열을 직접 갖지 않고 여기 상수를 참조한다.
 * 이렇게 모든 경로를 한곳에서 관리해 오타·중복을 방지하고 전체 라우트 네임스페이스를
 * 한눈에 본다. (Page 객체와 인자 헬퍼는 각 feature domain 에 유지된다.)
 *
 * 아래는 스타터 경로 하나만 둔다. feature 를 추가할 때마다 상수를 늘려간다.
 */
object AppRoutes {
    // onboarding
    const val SPLASH = "splash"
    const val ONBOARDING_INTRO = "onboarding/intro"
    const val MODEL_DOWNLOAD = "onboarding/model-download"
    const val FIRST_TRANSLATION = "onboarding/first-translation"

    const val HOME = "home" // 진입점(라이브러리 자리 임시)

    // import
    const val IMPORT_SHEET = "import/sheet"
    const val IMPORT_WEBNOVEL = "import/webnovel"
}

/**
 * 스타터 홈 페이지. 실제 feature 를 만들 때 각 feature/domain 모듈로 Page 객체를 옮긴다.
 */
object HomePage : Page {
    const val PATH = AppRoutes.HOME

    override fun toRoute(): NavRoute = NavRoute(PATH)
}
