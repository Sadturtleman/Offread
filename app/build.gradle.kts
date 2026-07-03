plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.android.offread"
    compileSdk {
        version =
            release(37) {
                minorApiLevel = 0
            }
    }

    defaultConfig {
        applicationId = "com.android.offread"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // :app 은 컴포지션 루트 + core·feature 모듈 집계점. Hilt 가 모든 @Module/@HiltViewModel 바인딩을 모은다.
    implementation(project(":core:entity"))
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(project(":core:datastore"))
    implementation(project(":core:database"))
    implementation(project(":onboarding:domain"))
    implementation(project(":onboarding:data"))
    implementation(project(":onboarding:presentation"))
    implementation(project(":library:domain"))
    implementation(project(":library:data"))
    implementation(project(":library:presentation"))
    implementation(project(":importer:domain"))
    implementation(project(":importer:data"))
    implementation(project(":importer:presentation"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // navigation3 단일 백스택 + ViewModel 스코프 데코레이터
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    // GenericNavKey(@Serializable) 백스택 직렬화 런타임
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.konsist)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
