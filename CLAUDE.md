# Newt Android library

The shared Newt look (`newt-ui`), the shared test helpers (`newt-test`) and the shared CI
workflow (`.github/workflows/newt-app.yml`) for every Newt app. The owner has low vision;
readability beats everything else. What's in it is in [README.md](README.md), the rules
in [NEWT_STYLE.md](NEWT_STYLE.md).

## Stack

Kotlin, Jetpack Compose, Material 3, published to GitHub Packages. Apps get changes
through Dependabot PRs, where their own tests run before anything merges.

## Checks: run them and show the output

CI runs the same checks on every PR (see `.github/workflows/build.yml`).

```sh
./gradlew recordRoborazziDebug   # GalleryTest: every shared piece in light, dark and 200% text
./gradlew lintDebug
```

Paste the result. Don't say "tests pass" without the output.

## Rules that are easy to break

- **Every app uses this.** A changed default (a color, a size, a spacing) changes every
  app. Say in the PR which apps will look different, and release it as
  "Releasing a new version" in the README says. A breaking change is a new major version.
- **7:1 contrast** for every text color, **no text under 16sp**, buttons at least 56dp
  and rows at least 48dp tall, words next to icons, 200% text and TalkBack. The shared
  pieces set the floor for every app, and `newt-test` is what enforces it there.
- **`newt-app.yml` runs in every app with the signing key.** Keep actions pinned to full
  commit SHAs, permissions as small as they can be, and nothing that runs code from a PR
  with secrets.
- **README.md is the manual** for whoever uses the library. Update it when a piece is
  added or changed.

## Before opening a PR, and after every push to one

1. Run the checks above.
2. **Review it.** Run `/code-review` on the branch with [REVIEW.md](REVIEW.md) as the rules,
   plus `/security-review`. Fix what they find, or say why not. Check every item under
   "What was asked" against what the code really does.
3. Open the PR with every section of the template filled in (What was asked, What
   changes, Testing, Risk, How to revert), then **post the review as a PR comment**:
   start with the count ("Nothing to fix." or "1 important, fixed in abc123."), then each
   finding with its file and line, its tag, and what was done about it. The owner reads
   the review there.
4. **Every later push to the PR gets the same review** of what changed since the last
   one, however small, posted as a new comment that names the commits it covers and the
   reviews that ran. If a review was skipped, the comment says so.

Merge only after the owner says "merge it". Apps pick up a new version through their own Dependabot PRs.
