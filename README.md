# Newt Android library

The shared Newt look for every Newt app ([Newt Phone](https://github.com/sec-newt/newt-phone),
[Newt Home](https://github.com/sec-newt/newt-home), and apps made from
[newt-android-template](https://github.com/sec-newt/newt-android-template)). Change it here
once, and every app gets the change through a Dependabot pull request, where its own
screenshot and accessibility tests run before anything merges.

| Module | What's in it | How an app adds it |
|---|---|---|
| `newt-ui` | `NewtTheme`, `NewtFonts` (the three bundled fonts), headers, section headings, card titles, `neonOutline()`, `neonFieldColors()` | `implementation("io.github.secnewt:newt-ui:1.0.0")` |
| `newt-test` | `newtScreenshot(...)`: a screenshot in the Newt theme plus accessibility checks, for screen tests | `testImplementation("io.github.secnewt:newt-test:1.0.0")` |

The rules behind the look are in [NEWT_STYLE.md](NEWT_STYLE.md). Every shared piece is
pictured in light, dark and 200% text by `GalleryTest`; CI uploads the pictures as the
**screenshots** artifact on every pull request.

## Using it in an app

GitHub Packages needs a login even for public packages, so `settings.gradle.kts` adds the
repository with the token GitHub Actions already has, and only for Newt packages:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        exclusiveContent {
            forRepository {
                maven("https://maven.pkg.github.com/sec-newt/newt-android-lib") {
                    credentials {
                        username = System.getenv("GITHUB_ACTOR")
                        password = System.getenv("GITHUB_TOKEN")
                    }
                }
            }
            filter { includeGroup("io.github.secnewt") }
        }
    }
}
```

`exclusiveContent` means Newt packages come only from here, and nothing else is ever
looked up here, so a look-alike package elsewhere can't sneak in.

## Releasing a new version

1. Merge the change (its pull request shows the gallery screenshots).
2. Publish a release with a version tag:
   `gh release create v1.1.0 -R sec-newt/newt-android-lib --generate-notes`
3. CI checks everything again and publishes `1.1.0`. Each app gets a Dependabot pull request
   within a week.

Use the middle number for additions (`1.1.0`), the last for fixes (`1.0.1`), and the first
when an app would have to change its code (`2.0.0`).
