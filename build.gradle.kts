plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.roborazzi) apply false
}

// The release tag (v1.2.0 → 1.2.0) sets the version; local builds are never published.
val libVersion = System.getenv("LIB_VERSION") ?: "0.0.0-local"

subprojects {
    group = "io.github.secnewt"
    version = libVersion

    // Both modules publish the same way, to this repository's GitHub Packages.
    plugins.withId("com.android.library") {
        apply(plugin = "maven-publish")
        extensions.configure<com.android.build.api.dsl.LibraryExtension> {
            publishing {
                singleVariant("release") {
                    withSourcesJar()
                }
            }
        }
        afterEvaluate {
            extensions.configure<PublishingExtension> {
                publications {
                    register<MavenPublication>("release") {
                        artifactId = project.name
                        from(components["release"])
                    }
                }
                repositories {
                    maven {
                        name = "GitHubPackages"
                        url = uri("https://maven.pkg.github.com/sec-newt/newt-android-lib")
                        credentials {
                            username = System.getenv("GITHUB_ACTOR")
                            password = System.getenv("GITHUB_TOKEN")
                        }
                    }
                }
            }
        }
    }
}
