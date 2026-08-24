import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.android.offread.translate.data"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    implementation(project(":translate:domain"))
    implementation(project(":core:database"))
    // F-003/F-029: 설치된 모델 언어쌍을 DataStore 에 영속한다.
    implementation(project(":core:datastore"))
    // F-020: 번역에 주입할 확정 용어를 용어맵에서 읽는다.
    implementation(project(":terms:domain"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // F-020 온디바이스 번역 엔진
    implementation(libs.mlkit.translate)
    implementation(libs.mediapipe.tasks.genai)
    implementation(libs.litertlm.android)

    // F-022 선번역 큐
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
