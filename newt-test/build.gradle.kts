plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.roborazzi)
}

// Test helpers: screenshots in light, dark and 200% text, plus accessibility checks.
// Apps add this with testImplementation, so it never ships inside an app.
android {
    namespace = "io.github.secnewt.test"
    compileSdk = 37

    defaultConfig {
        minSdk = 30
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.testLogging {
                    events("failed")
                    exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
                }
            }
        }
    }
    lint {
        abortOnError = true
        htmlReport = true
    }
}

dependencies {
    api(project(":newt-ui"))
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.ui.test.junit4)
    api(libs.roborazzi)
    api(libs.roborazzi.junit.rule)
    api(libs.roborazzi.accessibility.check)

    // The library's own gallery test (screenshots of every shared component).
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.junit)
    testImplementation(libs.robolectric)
    debugImplementation(libs.androidx.ui.test.manifest)
}
