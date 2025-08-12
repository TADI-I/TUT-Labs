

plugins {
    alias(libs.plugins.android.application) // Assuming you have this alias or use id("com.android.application")
    alias(libs.plugins.kotlin.android)     // Assuming you have this alias or use id("org.jetbrains.kotlin.android")
    alias(libs.plugins.jetbrains.kotlin.compose) // Use the chosen, correct alias
    id("com.google.gms.google-services")
    // NO direct id("org.jetbrains.kotlin.plugin.compose") here
}

android {
    namespace = "com.tadiwanashe.tutictlabs"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tadiwanashe.tutictlabs"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
    composeOptions {
        // Match with your Kotlin version
        // Check https://developer.android.com/jetpack/androidx/releases/compose-kotlin
        kotlinCompilerExtensionVersion = "1.5.7" // For Kotlin 2.0.0
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Make sure to use aliases for your libraries too, for consistency
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // Add navigation and viewmodel compose if you have them in your toml
    // implementation(libs.androidx.navigation.compose)
    // implementation(libs.androidx.lifecycle.viewmodel.compose)
    // implementation(libs.androidx.compose.runtime.livedata)

    implementation("com.google.accompanist:accompanist-swiperefresh:0.27.0")
// Check for latest version
    // Firebase (ensure these are in your libs.versions.toml if you want to use aliases)
    implementation(platform("com.google.firebase:firebase-bom:32.5.0")) // Consider adding this BOM to toml
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Coroutines (ensure these are in your libs.versions.toml if you want to use aliases)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.ui.text)
    implementation(libs.ui.graphics)
    implementation(libs.androidx.compose.material)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.androidx.foundation)
    implementation(libs.ads.mobile.sdk)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}