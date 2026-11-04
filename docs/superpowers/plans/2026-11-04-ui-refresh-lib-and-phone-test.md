# Newt UI refresh: library 1.4.0 + Newt Phone test build — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the shared low-vision pieces to `newt-ui` (release 1.4.0, no visual change to any app), then build a Newt Phone test version whose Settings, new Unknown callers page and Recents use them, so the owner can try the new look on his phone.

**Architecture:** Everything new in `newt-ui` is additive and opt-in: new composables in three new files, plus `NewtReadingTypography` (the 16sp floor), which screens opt into with `NewtReadingText { }`. Nothing existing changes size or color in 1.4.0. `newtScreenshot` gains an opt-in `minTextSp` check. Newt Phone moves to 1.4.0 and wraps only Settings, Unknown callers and Recents in `NewtReadingText`.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3 (BOM 2026.09.00), Robolectric 4.17 + Roborazzi 1.76.0 screenshot/accessibility tests, GitHub Packages.

**Spec:** `docs/superpowers/specs/2026-11-04-ui-refresh-design.md` (with mockups in `docs/superpowers/specs/2026-11-04-ui-refresh/`). Approved scope: library pieces + the Newt Phone test (Settings + Recents). Library 2.0.0 and the other apps are **out of scope** for this plan.

## Global Constraints

- **No visual change to existing library output in 1.4.0.** `NewtTypography`, `neonOutline()` (still 1.5dp default), `SectionLabel`, `NewtPromptLine` and `terminalTypography`'s output for existing styles stay pixel-identical. Gallery screenshots that exist today must not change.
- The new pieces use **2dp** borders (2.5dp for buttons and inputs), a 6dp edge stripe, and `surfaceContainerLow` as the lifted surface.
- Text: no style used by new pieces is under **16sp** at font scale 1. `NewtReadingTypography` sizes: `bodySmall`, `bodyMedium`, `labelSmall`, `labelMedium`, `labelLarge` 16sp; `bodyLarge` 17sp; `titleSmall` 17sp; `titleMedium` 19sp; `titleLarge` 21sp; line height = size × 1.3; display/headline as in `NewtTypography`.
- Every text color used by the new pieces is **≥ 7:1** against its background in both `NewtNight` and `NewtDay`.
- Touch targets ≥ 48dp. Buttons 58dp tall (64dp when `large`). Choice segments 54dp tall.
- Screen readers: every switch row reads its title and state once (`Role.Switch`). Choices read as radio buttons. No two nodes on a screen read the same.
- No new dependencies, permissions or internet use. Kotlin 17 target, `minSdk 30`.
- Copy is plain language. Buttons always have words.
- The owner's merge rule: nothing merges and no release is published until he says "merge it".
- Local commands need `local.properties` with `sdk.dir=/home/nk/Android/Sdk` (gitignored; already present in the lib). Gradle prints harmless "Failed connection handshake" lines; judge success by the exit code and test results.

## Review Focus

1. **The 200% font scale.** Values, chips and switches have to wrap under the title, not squash it or clip. Every new-piece gallery test runs at `fontScale = 2f`, and `titleNeverOverflowsAt200pct` (Task 3) and `chipNeverEllipsizes` (Task 4) check `TextLayoutResult.hasVisualOverflow == false` through the `GetTextLayoutResult` action.
2. **The light theme.** Gradient "ON" and selected states need light text on `NewtDay`'s dark blue/purple, not the dark-mode `#0A0B10`. `NewtTonesTest` checks every tone/foreground pair for 7:1 in both schemes.
3. **The terminal look.** `NewtReadingText` inside `NewtLookTheme(TERMINAL)` must keep JetBrains Mono, with the 16sp floor still holding after the ×0.94 shrink. There's a gallery test in the terminal look, plus a `minTextSp = 16f` assertion.
4. **The CUSTOM protection level.** When the caller rules were changed by hand, `NewtChoice` shows no segment selected, and the value reads "Custom". Covered by a phone test.
5. **Announce permission flow.** Choosing Always or Headphones in the new choice must still go through `onAnnounceModeChange`, which asks for permissions first. The "Turning this on asks for…" line must be visible while Announce is Off. Covered by a phone test.

---

## Part A — Library (`/home/nk/Git/newt-android-lib`, branch `ui-refresh-lib` from `main`, with the design docs cherry-picked or merged in)

Tests for all new pieces live in a new test file `newt-test/src/test/java/io/github/secnewt/test/PiecesTest.kt`, set up exactly like `GalleryTest` (Robolectric, `@GraphicsMode(NATIVE)`, `@Config(sdk = [34], qualifiers = Pixel7)`). New gallery screens are added to `GalleryTest`.

Run one test: `./gradlew -q :newt-test:testDebugUnitTest --tests '<Class>.<method>'`, then check the exit code is 0.
Record screenshots: `./gradlew -q recordRoborazziDebug`. Images land in `newt-test/build/outputs/roborazzi/` or `newt-test/build/screenshots/`, depending on the call. Look at them.

### Task 1: Reading typography, tones and gradient

**Files:**
- Modify: `newt-ui/src/main/java/io/github/secnewt/ui/NewtTheme.kt`
- Modify: `newt-ui/src/main/java/io/github/secnewt/ui/Look.kt` (`terminalTypography` becomes `internal` and gets the clamp)
- Create: `newt-ui/src/main/java/io/github/secnewt/ui/Tones.kt`
- Create test: `newt-ui/src/test/java/io/github/secnewt/ui/TonesTest.kt` (plain JUnit, no Robolectric)
- Test: `newt-test/src/test/java/io/github/secnewt/test/PiecesTest.kt`

**Interfaces — Produces:**
- `val NewtReadingTypography: Typography` (sizes in Global Constraints, fonts as `NewtTypography`)
- `@Composable fun NewtReadingText(content: @Composable () -> Unit)`: re-provides `MaterialTheme` with the same color scheme and `NewtReadingTypography`, or `terminalTypography(NewtReadingTypography)` when `terminalLook()`
- `enum class NewtTone { Good, Danger, Info, Neutral }`
- `@Composable fun NewtTone.color(): Color`. Dark: Good `0xFF9ECE6A`, Danger `colorScheme.error`, Info `colorScheme.primary`, Neutral `colorScheme.onSurfaceVariant`. Light: Good `0xFF2D5A0A`, others the same roles.
- `@Composable fun newtGradient(): Brush`: `Brush.linearGradient(primary, secondary)` (the same colors `neonOutline` uses)
- `@Composable fun onNewtGradient(): Color`: `Color(0xFF0A0B10)` in dark, `Color.White` in light
- `internal fun contrastRatio(a: Color, b: Color): Float`: the WCAG relative-luminance ratio

- [ ] **Step 1: Write the failing tests**

`TonesTest`:
- `everyToneIsSevenToOneInBothSchemes`: for `NewtNight` and `NewtDay`, assert `contrastRatio(toneColor, scheme.background) >= 7f` and `contrastRatio(toneColor, scheme.surfaceContainerLow) >= 7f` for all four tones. Use a non-composable helper `toneColor(tone, scheme, dark)` that `NewtTone.color()` also calls.
- `textOnGradientIsSevenToOne`: `contrastRatio(onGradient, scheme.primary) >= 7f` and the same against `scheme.secondary`, for both schemes.
- `readingTypographyFloor`: every style in `NewtReadingTypography` has `fontSize.value >= 16f`; `titleLarge == 21.sp`; `bodyLarge == 17.sp`.
- `terminalClampKeepsOldSizes`: for every style in `NewtTypography`, `terminalTypography(NewtTypography).<style>.fontSize` equals `base.fontSize * 0.94f` (unchanged behavior).
- `terminalClampHoldsFloor`: every style in `terminalTypography(NewtReadingTypography)` is `>= 16.sp`.

- [ ] **Step 2: Run them and see them fail** (with unresolved references): `./gradlew -q :newt-ui:testDebugUnitTest`. Add `testImplementation(libs.junit)` if needed; it's already present.

- [ ] **Step 3: Implement.** Clamp in `terminalTypography`: a style's new size is `fontSize * 0.94f`, raised to 16sp only when the base size is ≥ 16sp, so existing styles (none of which fall in 16–17.02sp) don't change.

- [ ] **Step 4: Run the tests until they pass.** Also run `./gradlew -q :newt-test:testDebugUnitTest --tests '*GalleryTest*'`. Expected: it passes and the existing gallery images are unchanged (compare against `main` with `git stash` or a second checkout if in doubt).

- [ ] **Step 5: Commit** `newt-ui: reading typography (16sp floor), tones, gradient helpers`

### Task 2: Opt-in text-size check in `newtScreenshot`

**Files:**
- Modify: `newt-test/src/main/java/io/github/secnewt/test/NewtScreenshots.kt`
- Test: `newt-test/src/test/java/io/github/secnewt/test/TextSizeCheckTest.kt`

**Interfaces — Produces:**
- `newtScreenshot(name, dark, fontScale = 1f, theme = …, minTextSp: Float? = null, content)`: a new parameter before `content`. When non-null and `fontScale == 1f`, call `assertTextAtLeast(minTextSp)` after the accessibility check.
- `fun ComposeContentTestRule.assertTextAtLeast(minSp: Float)`: walks `onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult), useUnmergedTree = true)`, gets each node's `TextLayoutResult`, and checks `layoutInput.style.fontSize` plus every span style's `fontSize` that `isSp`. Fails with one `AssertionError` listing every offender as `"<text>" is <n>sp, needs <minSp>sp`. Text sized in `em` fails as well, with the message `"<text>" uses em; use sp`.

- [ ] **Step 1: Write the failing tests**: `smallTextFails` (a `Text("tiny", fontSize = 12.sp)` → `assertThrows(AssertionError::class.java) { newtScreenshot("t-small", dark = true, minTextSp = 16f) {…} }`, and the message contains `"tiny" is 12`), `bigTextPasses` (16sp and 21sp texts pass), `spanCounts` (an `AnnotatedString` with a 12sp span fails), `skippedAtBigFontScale` (12sp at `fontScale = 2f` with `minTextSp` set doesn't fail).
- [ ] **Step 2: Run and see them fail:** `./gradlew -q :newt-test:testDebugUnitTest --tests '*TextSizeCheckTest*'`
- [ ] **Step 3: Implement** as described in Interfaces.
- [ ] **Step 4: Run and see them pass.**
- [ ] **Step 5: Commit** `newt-test: opt-in minimum text size check`

### Task 3: Settings pieces: switch, rows, card, info, rule heading

**Files:**
- Create: `newt-ui/src/main/java/io/github/secnewt/ui/Settings.kt`
- Test: `PiecesTest.kt`, plus gallery additions in `GalleryTest.kt` (a new `SettingsGallery()` with tests `settingsGalleryDark`, `settingsGalleryLight`, `settingsGallery200pct`, `settingsGalleryTerminalDark`, all with `minTextSp = 16f` except the 200% one, and the content wrapped in `NewtReadingText`)

**Interfaces — Consumes:** `NewtTone`, `NewtTone.color()`, `newtGradient()`, `onNewtGradient()`, `NewtReadingText` (Task 1).
**Produces:**
- `@Composable fun NewtSwitch(checked: Boolean, modifier: Modifier = Modifier)`: visual only (80×40dp, the words **ON** / **OFF** inside in mono 16sp bold, gradient with an `onNewtGradient` thumb and text when on, a 2.5dp `onSurfaceVariant` outline when off). It's `clearAndSetSemantics {}`, because the row owns the semantics.
- `@Composable fun NewtSettingsCard(title: String, icon: ImageVector? = null, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit)`: `surfaceContainerLow`, 2dp `outlineVariant` border, 18dp corners. The heading is in `NewtFonts.Display` 19sp, `colorScheme.tertiary` color, with `heading()` semantics.
- `@Composable fun NewtSettingRow(title: String, modifier: Modifier = Modifier, hint: String? = null, trailing: (@Composable () -> Unit)? = null)`: min height 62dp, horizontal padding 14dp, and a 2dp `outlineVariant` line along its top. The title is `titleMedium` bold, the hint `bodyMedium` in `onSurfaceVariant`. If the trailing content doesn't fit beside the title at the current width, it goes below it (use a `FlowRow`, or measure and wrap).
- `@Composable fun NewtSwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier, hint: String? = null)`: the whole row is `toggleable(role = Role.Switch)`, with `NewtSwitch` trailing.
- `@Composable fun NewtValueRow(title: String, value: String?, onClick: () -> Unit, modifier: Modifier = Modifier, hint: String? = null, tone: NewtTone = NewtTone.Info)`: `clickable(role = Role.Button)`, trailing `NewtStateChip(value, tone)` (when value isn't null) followed by a › chevron (decorative).
- `@Composable fun NewtLockedRow(title: String, value: String, reason: String, modifier: Modifier = Modifier)`: not clickable. Trailing is a dashed Good-tone chip "🔒 $value" (use `Icons.Filled.Lock`, not an emoji). Semantics: `contentDescription = "$title, $value. $reason"`.
- `@Composable fun NewtInfo(details: String, modifier: Modifier = Modifier, label: String = "What's this?")`: an ⓘ row (`Icons.Outlined.Info` or `Icons.Filled.Info`, from core) that expands `details` in place. `Role.Button`, with an "expanded/collapsed" state description.
- `@Composable fun NewtRuleHeading(text: String, modifier: Modifier = Modifier)`: uppercase `NewtFonts.Mono` 16sp bold, letter-spacing 0.1em, `tertiary` color, followed by a 2dp `outlineVariant` rule filling the row. `heading()` semantics, and the content description is `text` (not uppercased).

- [ ] **Step 1: Write the failing tests** in `PiecesTest`:
  - `switchRowReadsTitleAndStateOnce`: in `NewtSwitchRow("Favorites", checked = true, …)`, `onNodeWithText("Favorites", useUnmergedTree = false)` has `Role.Switch`, `assertIsOn()`, and there's exactly one node with `SemanticsProperties.ToggleableState`.
  - `switchRowToggles`: clicking the row calls `onCheckedChange(false)` when it's checked.
  - `valueRowIsButtonWithValue`: it has `Role.Button`, and its merged text contains both the title and the value.
  - `lockedRowIsNotClickable`: `assertHasNoClickAction()`, and the content description equals `"After 911, Always. Every call rings for an hour after you call 911."`.
  - `infoExpands`: `details` doesn't exist before the click and is displayed after it.
  - `titleNeverOverflowsAt200pct`: at `fontScale = 2f`, with a 340dp-wide box, `NewtValueRow("Unknown callers", "Balanced", …)` and `NewtSwitchRow("Quiet in Do Not Disturb", …)` both report `TextLayoutResult.hasVisualOverflow == false` for the title.
- [ ] **Step 2: Run and see them fail.**
- [ ] **Step 3: Implement.** Also add the `SettingsGallery` composable: a card with two switch rows (on/off), a value row, a locked row and an info row, plus a rule heading "Today".
- [ ] **Step 4: Run** `PiecesTest` and `GalleryTest` until they pass, then `./gradlew -q recordRoborazziDebug` and **look at** the four new settings-gallery images: ON/OFF are legible, the light mode isn't washed out, and at 200% nothing clips.
- [ ] **Step 5: Commit** `newt-ui: settings card, rows, ON/OFF switch, info, rule heading`

### Task 4: Choice, pick list, state chip, summary, stripe

**Files:**
- Create: `newt-ui/src/main/java/io/github/secnewt/ui/Choices.kt`
- Test: `PiecesTest.kt`, plus a `ChoicesGallery()` in `GalleryTest` with the same four variants as Task 3

**Interfaces — Consumes:** Task 1 tones and gradient. **Produces:**
- `@Composable fun NewtStateChip(text: String, tone: NewtTone, modifier: Modifier = Modifier, icon: ImageVector? = null)`: a 2.5dp border in the tone color, text in the tone color, mono 16sp bold, 12dp corners, `wrapContentWidth`, single line, and no ellipsis (it wraps whole).
- `@Composable fun <T> NewtChoice(options: List<T>, selected: T?, label: (T) -> String, onSelect: (T) -> Unit, modifier: Modifier = Modifier, explanation: String? = null)`: 2–3 segments, 54dp high, with a 2.5dp primary outer border. The selected segment gets the gradient fill and `onNewtGradient` text. `selectableGroup()` + `selectable(role = Role.RadioButton)`. `explanation` (when not null) appears below it in `bodyMedium`. `selected == null` → nothing filled. `require(options.size in 2..3)`.
- `@Composable fun <T> NewtPickList(options: List<T>, selected: T?, label: (T) -> String, hint: (T) -> String?, onSelect: (T) -> Unit, modifier: Modifier = Modifier)`: rows like `NewtSettingRow`, a 30dp circle (filled gradient with a ✓ when selected), and the hint shown **only** under the selected row. Radio semantics.
- `@Composable fun NewtSummary(label: String, text: String, modifier: Modifier = Modifier, tone: NewtTone = NewtTone.Good)`: `surfaceContainerHigh`, a 6dp left stripe in the tone color, the label in mono 16sp bold letter-spaced uppercase tone color, and `text` in `bodyLarge`. Semantics are merged into `"$label. $text"`.
- `fun Modifier.newtStripe(color: Color): Modifier`: draws a 6dp leading-edge bar and a horizontal tint (`color` at alpha 0.13 → transparent at 70%) behind the content. Decoration only; it changes no semantics.

- [ ] **Step 1: Write the failing tests**:
  - `choiceSelectsAndReadsAsRadio`: three options. Clicking "Strict" calls `onSelect(STRICT)`, and each segment has `Role.RadioButton` and `assertIsSelected()` matching `selected`.
  - `choiceWithNothingSelected`: `selected = null` → `assertIsNotSelected()` on all segments.
  - `choiceRejectsFourOptions`: four options throw `IllegalArgumentException`.
  - `pickListShowsOnlySelectedHint`: the hint for the selected option exists, and the hints for the others don't.
  - `summaryReadsAsOneSentence`: one node with the content description `"Right now. Favorites ring on silent."`.
  - `chipNeverEllipsizes`: `NewtStateChip("Would silence", …)` at 200% in a 120dp box reports no visual overflow (it wraps).
- [ ] **Step 2: Run and see them fail.**
- [ ] **Step 3: Implement** these, and add `ChoicesGallery`: a Watch/Block choice, an Off/Balanced/Strict choice with an explanation, a 3-option pick list, chips in all four tones, a summary, and a row with a red stripe.
- [ ] **Step 4: Run** the tests until they pass, record the screenshots, and look at the gallery images.
- [ ] **Step 5: Commit** `newt-ui: choice, pick list, state chips, summary, edge stripe`

### Task 5: Buttons, action grid, steps

**Files:**
- Create: `newt-ui/src/main/java/io/github/secnewt/ui/Actions.kt`
- Test: `PiecesTest.kt`, plus an `ActionsGallery()` in `GalleryTest` (same four variants)

**Interfaces — Produces:**
- `@Composable fun NewtButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, large: Boolean = false)`: outlined with a 2.5dp primary border, 58/64dp high, 16dp corners, text 19sp bold (22sp when `large`).
- `@Composable fun NewtPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, tone: NewtTone = NewtTone.Info, large: Boolean = false)`: Info → gradient fill + `onNewtGradient`. Good → `NewtTone.Good.color()` fill + `onNewtGradient` text (check it's 7:1 in `TonesTest`, and if light-mode Good fails, use white text). Danger → a 3dp error outline with error-color text (never a filled red). Neutral → the same as `NewtButton`.
- `data class NewtAction(val label: String, val onClick: () -> Unit, val icon: ImageVector? = null, val hint: String? = null)`
- `@Composable fun NewtActionGrid(actions: List<NewtAction>, modifier: Modifier = Modifier)`: two columns, 8dp gaps, tiles with min height 66dp, `surfaceContainerHigh`, a 2dp `outlineVariant` border, the label 18sp bold and the hint `bodyMedium`.
- `enum class NewtStepState { Done, Now, ToDo }`
- `data class NewtStep(val title: String, val state: NewtStepState, val hint: String? = null, val action: (@Composable () -> Unit)? = null)`
- `@Composable fun NewtSteps(steps: List<NewtStep>, modifier: Modifier = Modifier)`: a 36dp number circle (Done = a Good fill with ✓, Now = a gradient fill, ToDo = an outline), with `action` shown only for the Now step. Each step's semantics: `"Step $n of $total, ${title}, done|current|not yet"`.

- [ ] **Step 1: Write the failing tests**: `buttonsAreTallEnough` (`assertHeightIsAtLeast(58.dp)`, and 64dp when `large`), `actionGridTilesAreButtons` (each tile has `Role.Button` and its label), `stepsReadTheirState` (content descriptions are `"Step 1 of 3, Install a map app, done"` and so on), `onlyCurrentStepShowsAction`.
- [ ] **Step 2: Run and see them fail.**
- [ ] **Step 3: Implement** these, and add `ActionsGallery`: all button tones, a six-tile action grid, and three steps.
- [ ] **Step 4: Run** the tests until they pass, record the screenshots, and look at them.
- [ ] **Step 5: Commit** `newt-ui: worded buttons, action grid, numbered steps`

### Task 6: Docs, pull request, release 1.4.0

**Files:**
- Modify: `README.md` (the module table lists the new pieces in one line) and `NEWT_STYLE.md` (add a "New screens use" paragraph: the pieces, the 16sp floor, `NewtReadingText`. The "8% smaller" line stays until 2.0.0, so note that it's going away.)

- [ ] **Step 1: Run everything:** `./gradlew -q recordRoborazziDebug lintDebug`. Expected: exit 0, and the existing gallery images (`gallery-dark/light/200pct/terminal-*`) are byte-identical to `main`. Compare them with `git diff --stat` after copying the images, or with `cmp` against a build from `main`.
- [ ] **Step 2: Commit** the docs. Push `ui-refresh-lib` and open a PR titled "UI refresh pieces (1.4.0): ON/OFF switch, setting rows, choices, steps", with the gallery screenshots called out and the PR attribution footer. Link the PR.
- [ ] **Step 3: Wait for the owner's "merge it".** Then merge, and run `gh release create v1.4.0 -R sec-newt/newt-android-lib --generate-notes`. Confirm the publish job succeeded: `gh run list -R sec-newt/newt-android-lib -L 1`.

---

## Part B — Newt Phone test build (`/home/nk/Git/newt-phone`, branch `ui-refresh-test` from `main`)

Builds need the library from GitHub Packages:
`GITHUB_ACTOR=nk GITHUB_TOKEN=$(gh auth token) ./gradlew -q …` (the token only goes into the environment).
Tests: `./gradlew -q :app:testDebugUnitTest --tests '<Class>*'`. Screenshots: `./gradlew -q recordRoborazziDebug`.
Screen tests wrap content the way the existing `render(...)` helpers do. Add `NewtReadingText { }` inside `NewtLookTheme` in the tests for the screens below, matching the app.

### Task 7: Move to 1.4.0, the reading text wrapper, and the summary sentence

**Files:**
- Modify: `gradle/libs.versions.toml` (`newtLib = "1.4.0"`)
- Modify: `app/src/main/java/io/github/secnewt/dialer/MainActivity.kt` (wrap the `Screen.SETTINGS`, new `Screen.UNKNOWN_CALLERS` and `Tab.RECENTS` content in `NewtReadingText { }`)
- Create: `app/src/main/java/io/github/secnewt/dialer/ui/SettingsSummary.kt`
- Test: `app/src/test/java/io/github/secnewt/dialer/ui/SettingsSummaryTest.kt` (plain JUnit)

**Interfaces — Produces:** `fun settingsSummary(spam: SpamSettings, ring: RingSettings, announce: AnnounceSettings, roleHeld: Boolean?): String`. The text is exact, with the sentences joined by a space:
- Ringing: both `starredRingThrough` and `repeatRingThrough` → `"Favorites and repeat callers ring on silent."`; only starred → `"Favorites ring on silent."`; only repeat → `"Repeat callers ring on silent."`; neither → `"Nobody rings on silent."`
- Spam: `roleHeld == false` → `"Spam protection is off."`; `level == OFF` → `"Spam protection is off."`; `observeOnly` → `"Spam is watched, not blocked."`; any action `BLOCK` → `"Suspicious calls are blocked or silenced."`; otherwise → `"Suspicious calls are silenced."`
- Announce: `mode != OFF` → `"Callers are announced."`; otherwise nothing.

- [ ] **Step 1: Write the failing tests** for each sentence branch, plus one combined case: `settingsSummary(SpamSettings(observeOnly = true), RingSettings(), AnnounceSettings(), true) == "Favorites and repeat callers ring on silent. Spam is watched, not blocked."`
- [ ] **Step 2: Run and see them fail.**
- [ ] **Step 3: Implement** it, bump the library version, and add the wrappers (`Screen.UNKNOWN_CALLERS` is added in Task 9; for now, wrap SETTINGS and RECENTS).
- [ ] **Step 4: Run** `./gradlew -q :app:testDebugUnitTest`. Expected: all pass (existing screenshots change because of the bigger text on two screens, which is expected).
- [ ] **Step 5: Commit** `Use newt-ui 1.4.0; bigger reading text on Settings and Recents; settings summary`

### Task 8: Settings main page

**Files:**
- Modify: `app/src/main/java/io/github/secnewt/dialer/ui/SpamSettingsScreen.kt` (main page only; the unknown-caller pieces move out in Task 9)
- Modify: `app/src/main/java/io/github/secnewt/dialer/ui/RingingSettings.kt` (`SwitchRow` → `NewtSwitchRow`; the quiet-hours time rows → `NewtValueRow`)
- Test: `app/src/test/java/io/github/secnewt/dialer/ui/SettingsScreensTest.kt`

**Interfaces — Consumes:** all of Part A, and `settingsSummary` (Task 7). **Produces:** `SpamSettingsScreen` gains `onOpenUnknownCallers: () -> Unit = {}` and `ringSettings`/`roleHeld` are used for the summary. All existing parameters stay.

The page, top to bottom (copy is exact):
1. `ScreenHeader("Settings", onBack)`
2. `NewtSummary("Right now", settingsSummary(...))`
3. If `isPhoneApp != null`: `NewtSettingsCard("Phone app")` holding the existing `PhoneAppCard` content, with its button as `NewtPrimaryButton`.
4. `NewtSettingsCard("Say who's calling", Icons.Filled.Call)`: `NewtChoice(AnnounceMode.entries, announce.mode, label = { "Off" | "Always" | "Headphones" }, onSelect = onAnnounceModeChange)`. While the mode is OFF, the explanation is `"Turning this on asks for Phone, Call log and Contacts access, used only on this phone."`; otherwise it's `announceModeExplanation(mode)`. When the mode isn't OFF, add `NewtSwitchRow("Quiet in Do Not Disturb", …)`, `NewtSwitchRow("Keep repeating", hint = "Until you answer", …)`, `NewtButton("Test the voice", …)` and `NewtValueRow("Voice and speed", null, …)` → TTS. `announceMessage`, when not null, shows above the choice.
5. `NewtSettingsCard("Ring on silent", Icons.Filled.Notifications)`: `RingingSection` rows, as switch rows with one-line hints: `"At alarm volume"`, `"Twice in 3 minutes"`, and for quiet hours `"Only those two ring, at night"`.
6. `NewtSettingsCard("Spam protection", Icons.Filled.Warning)`: `NewtChoice(listOf(true, false), settings.observeOnly, label = { if (it) "Watch" else "Block" }, onSelect = { onSettingsChange(settings.copy(observeOnly = it)) }, explanation = if (observeOnly) "Every call rings. Recents shows what would have been stopped." else "The rules are applied to real calls.")`, then `NewtValueRow("Unknown callers", levelName(level) /* CUSTOM → "Custom" */, onOpenUnknownCallers)` and `NewtValueRow("Block list", "$blockListSize", onOpenBlockList)`.
7. `NewtSettingsCard("Phone")`: `NewtSwitchRow("Keypad sounds", hint = "A tone for each key", …)`, then a `NewtValueRow(setting.title, null, hint = setting.detail)` for each `SOUND, CALLS, VOICEMAIL, ACCESSIBILITY`.

The protection-level choice, caller rows, my number, Let through and their dialogs leave this page (Task 9). The page uses 12dp spacing between cards.

- [ ] **Step 1: Update or write the failing tests**:
  - `summaryShowsAtTop`: `onNodeWithContentDescription("Right now. Favorites and repeat callers ring on silent. Spam is watched, not blocked.")` exists for the default settings.
  - `watchBlockFlipsObserveOnly`: clicking "Block" calls `onSettingsChange` with `observeOnly == false`.
  - `announceChoiceGoesThroughCallback`: clicking "Always" calls `onAnnounceModeChange(ALWAYS)` and nothing else (the permission flow stays in MainActivity).
  - `permissionLineShowsWhileOff`: with the mode OFF, the text `"Turning this on asks for Phone, Call log and Contacts access, used only on this phone."` exists.
  - `unknownCallersRowOpensPage`: clicking the "Unknown callers" row calls `onOpenUnknownCallers`, and the row's text includes "Balanced".
  - `customLevelReadsCustom`: with `custom` settings (rows changed by hand), the row value is "Custom".
  - Keep the existing screenshot test names (`settings_default_light`, `settings_custom_dark`, `settings_default_light_font200`, `settings_announce-on_*`, `terminal_settings_*`), pass `minTextSp = 16f` to the 100% ones through the `render` helper (call `composeRule.assertTextAtLeast(16f)` there), and remove the tests whose content moved to Task 9.
- [ ] **Step 2: Run and see them fail.**
- [ ] **Step 3: Implement** the page as listed.
- [ ] **Step 4: Run** `SettingsScreensTest` until it passes, record the screenshots, and **look at** `settings_*` next to mockup 1. Everything should be readable at 200%, and the light mode shouldn't be washed out.
- [ ] **Step 5: Commit** `Settings: Right now summary, cards, ON/OFF rows, Watch/Block`

### Task 9: Unknown callers page

**Files:**
- Create: `app/src/main/java/io/github/secnewt/dialer/ui/UnknownCallersScreen.kt` (moves `CallerType`, `CallerTypeRow`, `LetThroughSection`, `EXPECTING_HOURS`, and the caller/expecting dialogs out of `SpamSettingsScreen.kt`. `ChoiceDialog` becomes `internal` in `SpamSettingsScreen.kt` and is shared.)
- Modify: `MainActivity.kt`: add `UNKNOWN_CALLERS` to `enum class Screen`. Settings' `onOpenUnknownCallers = { screen = Screen.UNKNOWN_CALLERS }`. The new screen's back button goes to `Screen.SETTINGS`, and it's wrapped in `NewtReadingText`.
- Test: `app/src/test/java/io/github/secnewt/dialer/ui/UnknownCallersScreenTest.kt` (move the related tests from `SettingsScreensTest`: `expectingACallSetsATimeAndTurnsOff`, `callBacksCanBeTurnedOff`, `theChoiceSaysWhatTheRowMeans`, `newNumbersCanBeSetToRing`, `unknownCallersRows*`, `letThrough*`)

**Interfaces — Produces:** `@Composable fun UnknownCallersScreen(settings: SpamSettings, onSettingsChange: (SpamSettings) -> Unit, onBack: () -> Unit, nowMillis: Long = System.currentTimeMillis())`

The page, top to bottom:
1. `ScreenHeader("Unknown callers", onBack)`
2. `NewtChoice(listOf(OFF, BALANCED, STRICT), selected = settings.level.takeIf { it != CUSTOM }, label = ::levelName, onSelect = { onSettingsChange(settings.withLevel(it)) }, explanation = levelDescription(level))`. For CUSTOM the explanation is `"Custom: you changed the rules below by hand."`
3. `NewtSettingsCard("Numbers you don't know")`: a `NewtValueRow(type.title, actionLabel(action), onClick = open the existing dialog, tone = RING→Good, SILENCE→Info, BLOCK→Danger)` for each of the five types.
4. `NewtSettingsCard("Copycat check")`: the existing "My phone number" field, with the hint `"Spots numbers that copy yours. It stays on this phone."`
5. `NewtSettingsCard("Always let through", Icons.Filled.Check)`: `NewtSwitchRow("Call-backs", hint = "1 hour after you call a new number", …)`, `NewtValueRow("Expecting a call", <existing value text>, …)`, `NewtLockedRow("After 911", "Always", "Every call rings for an hour after you call 911.")`

- [ ] **Step 1: Write the failing tests** (the moved ones, re-pointed to the new screen, plus): `levelChoiceAppliesPreset` (clicking "Strict" → `onSettingsChange(settings.withLevel(STRICT))`), `customLevelSelectsNothing` (with custom settings, no segment is selected and the explanation is the Custom line), `chipsShowAction` (the `NEW_NUMBERS = RING` row's text contains "Ring"), `nineOneOneIsLocked` (the content description matches the `NewtLockedRow` text, with no click action), `backGoesToSettings` (in a MainActivity-level test if one exists; otherwise cover it with the callback). Screenshots: `unknown-callers_dark`, `unknown-callers_light`, `unknown-callers_dark_font200`, `terminal_unknown-callers_dark`, with `minTextSp = 16f` at 100%.
- [ ] **Step 2: Run and see them fail.**
- [ ] **Step 3: Implement** the screen and the navigation.
- [ ] **Step 4: Run** `./gradlew -q :app:testDebugUnitTest` until it all passes, then record the screenshots and look at them next to mockup 2.
- [ ] **Step 5: Commit** `Unknown callers: its own page with level choice, action chips, locked 911 row`

### Task 10: Recents

**Files:**
- Modify: `app/src/main/java/io/github/secnewt/dialer/ui/RecentsScreen.kt` (`StatusPanel`, `DayHeading`, `RecentRow`, `KindLine`, `SpamTag`)
- Modify: `app/src/main/java/io/github/secnewt/dialer/ui/DialerTabs.kt` (bottom nav labels at least 16sp; the active item's indicator uses `newtGradient()` with an `onNewtGradient()` icon)
- Test: `app/src/test/java/io/github/secnewt/dialer/ui/CallingScreensTest.kt`

The changes (mockup A):
- `StatusPanel`: no card. A `FlowRow` with two `NewtValueRow`-style chips, each a 48dp+ button: `"Spam: Watching" | "Spam: On" | "Spam: Off"` (opens spam protection) and `"Announce: <announceModeName>"` (opens settings). When `!roleHeld`, there's also a full-width `NewtPrimaryButton("Turn on spam protection")` below.
- `DayHeading` → `NewtRuleHeading(day)`.
- `RecentRow`: the name in `titleLarge` (21sp) bold, the details line in `bodyMedium` (16sp). Spam and missed-call rows get `Modifier.newtStripe(colorScheme.error)` and their text in error color. The call button is a 58dp rounded square (18dp corners) with a 2.5dp Good-tone border and a Good-tone icon, keeping its existing content description.
- `SpamTag` reads as `"⚠ Likely spam"` using `Icons.Filled.Warning`, in `bodyMedium` bold error color.

- [ ] **Step 1: Write the failing tests**: `spamRowHasWordsNotJustColor` (the spam row's merged text contains "Likely spam"), `statusChipsOpenTheirScreens` (clicking "Spam: Watching" calls `onOpenSpamProtection`, and clicking "Announce: Off" calls `onOpenSettings`), `callButtonIsBigEnough` (`assertHeightIsAtLeast(58.dp)`). Keep the screenshot names `recents_*` and `terminal_recents_*`, with `minTextSp = 16f` at 100%.
- [ ] **Step 2: Run and see them fail.**
- [ ] **Step 3: Implement** the changes.
- [ ] **Step 4: Run** `./gradlew -q :app:testDebugUnitTest` until it all passes, then record the screenshots and look at them next to mockup A.
- [ ] **Step 5: Commit** `Recents: status chips, rule headings, 21sp names, stripe on spam/missed, bigger call buttons`

### Task 11: Test build pull request

- [ ] **Step 1:** `./gradlew -q recordRoborazziDebug lintDebug assembleDebug`. Expected: exit 0.
- [ ] **Step 2:** Push `ui-refresh-test` and open a **draft** PR titled "UI refresh test: Settings, Unknown callers, Recents". The body lists what to try on the phone (open Settings and read the Right now box; flip a switch and confirm ON/OFF; open Unknown callers; check Recents at your normal text size) and says "Not for merging until you've lived with it." Add the attribution footer and link the PR.
- [ ] **Step 3:** When CI is green, give the owner the test APK from the PR's "Build test APK" artifact (`gh run download <run> -R sec-newt/newt-phone -n <apk artifact>`). Its file path goes in the handover message.

---

## Self-review notes

- Spec coverage for this scope: the new pieces (`NewtSwitch`, settings card/rows, `NewtStateChip`, `NewtChoice`, `NewtPickList`, `NewtSummary`, `newtStripe`, buttons, `NewtActionGrid`, `NewtSteps`, `NewtInfo`) are in Tasks 3–5. The 16sp check is Task 2. The type floor (opt-in) is Task 1. Phone mockups A, 1 and 2 are Tasks 8–10. Mockup 3 (the Calls screen) and the incoming-call screen (B) aren't in the approved test scope.
- Deferred to 2.0.0 (not this plan): the default typography change, `neonOutline` at 2dp, the `SectionLabel` restyle, `NewtPromptLine` at 16sp, and `minTextSp` on by default.
- **My calls:** chip tones on Unknown callers are Ring = green, Silence = blue, **Block = red**. The mockup showed Silence in red, but the real app also has Block, which needs to be the strongest. The bigger text applies only to the three redesigned screens, so the test isn't muddied by screens that haven't been redesigned yet. `NewtSwitch` is 80dp wide (the mockup showed 74dp) so ON/OFF fits at 16sp.
