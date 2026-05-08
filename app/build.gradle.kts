plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.debt"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.debt"
        minSdk = 25
        targetSdk = 35
        versionCode = 11
        versionName = "5.5" // 6.0 после добавления юзеров

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    applicationVariants.all {
        outputs.map { output ->
            val outputImpl = output as com.android.build.gradle.internal.api.BaseVariantOutputImpl

            val appName = "debt"

            val versionName = versionName ?: "unknown"
            val versionCode = versionCode.toString()
            val buildType = buildType.name

            val fileName = buildString {
                append(appName)
                append("-$buildType")
                append("-v$versionName")
                append("(vc$versionCode)")
                append(".apk")
            }

            outputImpl.outputFileName = fileName
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    // coil
    implementation(libs.coil.compose)

    // datastore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore)

    // api
    implementation(libs.retrofit)
    implementation(libs.logging.interceptor)
    implementation(libs.retrofit2.kotlin.coroutines.adapter)

    // gson
    implementation(libs.gson)
    implementation(libs.converter.gson)

    // threetenabp
    implementation(libs.threetenabp)

    // navigation
    implementation(libs.androidx.navigation.compose)

    // reflect
    implementation(libs.kotlin.reflect)

    // security
    implementation(libs.androidx.security.crypto)

    // paging
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    // koin
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // swiperefresh
    implementation(libs.accompanist.swiperefresh)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.ui)
    implementation(libs.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material)
    implementation(libs.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}