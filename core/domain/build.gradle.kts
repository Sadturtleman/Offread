plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
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
    implementation(project(":core:entity"))
    // NavRoute 직렬화(Json)·Helper 인터페이스가 노출하는 Flow 는 소비 모듈(core:ui, app)의
    // 공개 시그니처에도 나타나므로 api 로 전파한다.
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.coroutines.core)
}
