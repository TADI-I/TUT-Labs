

// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.jetbrains.kotlin.compose)   // Required for Kotlin 2.0+ Compose projects
    id("com.google.gms.google-services") version "4.4.3" apply false

    // Explicit Kotlin version declaration (optional but recommended)
   // id("org.jetbrains.kotlin.android") version "2.0.0" apply false
}