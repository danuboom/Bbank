// Top-level build file
plugins {
    id("com.android.application") version "8.11.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false

    // FIX: Use the Android-specific Compose plugin ID
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}