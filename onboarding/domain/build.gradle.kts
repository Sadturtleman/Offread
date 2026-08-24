plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    // LanguagePair 등 공유 커널과 Page/NavRoute 가 본 모듈의 공개 시그니처에 노출되므로 api 로 전파.
    api(project(":core:entity"))
    api(project(":core:domain"))
    // 번역 모델 카탈로그·다운로드 포트는 번역 코어가 소유한다(F-029).
    api(project(":translate:domain"))
    implementation(libs.kotlinx.coroutines.core)
    // UseCase 의 @Inject 생성자(런타임 Hilt 컴포넌트에서 제공). 도메인은 hilt 런타임 없이 annotation 만.
    implementation(libs.javax.inject)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
