plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

// The Newt look: theme, fonts and shared components. Apps depend on this.
android {
    namespace = "io.github.secnewt.ui"
    compileSdk = 37

    defaultConfig {
        // The lowest any Newt app needs (Newt Phone: Android 11).
        minSdk = 30
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    lint {
        abortOnError = true
        htmlReport = true
    }
}

dependencies {
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.ui)
    api(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    // Loads the bundled font to draw the ASCII newt.
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
}
