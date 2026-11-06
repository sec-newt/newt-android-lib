# UI refresh, library 2.0.0 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the UI refresh's look the default for every Newt app: the 16sp type floor, 2dp outlines, the rule-line `SectionLabel`, and a 16sp check that `newtScreenshot` runs by default. Release **2.0.0**.

**Architecture:** `NewtTypography` takes over `NewtReadingTypography`'s sizes, so every app's text grows the moment it moves to 2.0.0, with no screen code changed. `NewtReadingText` stays as a harmless, deprecated wrapper so Newt Phone keeps compiling. `newtScreenshot` checks for 16sp by default; apps opt out per test with `minTextSp = null` only while they fix a screen.

**Tech Stack:** Kotlin 2.4.21, Jetpack Compose (BOM 2026.09.00), Material 3, Robolectric 4.17 + Roborazzi 1.76.0.

**Spec:** `docs/superpowers/specs/2026-11-04-ui-refresh-design.md`, "Library work → Changes to what exists" and "Order of work" step 2.

## Global Constraints

- No text under **16sp** at the phone's default font size, in the clean and terminal looks.
- Sizes: `bodySmall`, `labelSmall`, `labelMedium`, `bodyMedium`, `labelLarge` → **16sp**; `bodyLarge` and `titleSmall` **17sp**; `titleMedium` **19sp**; `titleLarge` **21sp**; line height ×1.3. Display and headline styles don't change.
- The terminal look's ×0.94 mono shrink clamps at 16sp (already true in `terminalTypography`).
- `neonOutline()` default width is **2dp**. `NewtPromptLine` is **16sp**.
- No new palette, fonts, permissions or dependencies.
- Release tag `v2.0.0`. Nothing merges or gets released until the owner says "merge it".

## Review Focus

1. **The text-size check misses text hidden from screen readers** (`clearAndSetSemantics`), such as `NewtRuleHeading`'s visible word and `NewtSwitch`'s ON/OFF. Someone reading the README expects "fails any text under 16sp" to mean all text. Either the check sees that text or the docs say exactly what it skips. Task 3 adds a test.
2. **The terminal look at the floor:** a 16sp style must stay at 16sp in JetBrains Mono, not drop to 15.04. Task 1 tests every style in the terminal look.
3. **Two ⓘ controls on one screen** must not sound the same. `NewtInfo`'s state should name what opens ("What's this?, collapsed"), using the standard expanded/collapsed wording. Task 2 tests it.
4. **Byte-identical output is no longer the goal:** existing gallery screenshots change on purpose. The PR must show before/after for the old gallery. Task 4.
5. **An app that already passes `minTextSp = 16f` must still pass.** Phone's `renderScreen` passes it explicitly, so the new default must not change behavior there. Task 3 keeps the parameter's meaning.

---

### Task 1: The type floor becomes the default

**Files:**
- Modify: `newt-ui/src/main/java/io/github/secnewt/ui/NewtTheme.kt` (`TEXT_SCALE`, `scaled`, `NewtTypography`, `NewtReadingTypography`, `NewtReadingText`)
- Test: `newt-ui/src/test/java/io/github/secnewt/ui/TypographyTest.kt` (new)

**Interfaces:**
- Produces: `NewtTypography` with the floor sizes; `NewtReadingTypography` = `NewtTypography`, marked `@Deprecated("Same as NewtTypography since 2.0.0")`; `NewtReadingText` keeps its signature and is marked `@Deprecated("Not needed since 2.0.0: NewtTheme has the 16sp floor")`.

- [ ] **Step 1: Write the failing test** `TypographyTest`:
  - `floorSizes`: assert `NewtTypography.bodySmall/labelSmall/labelMedium/bodyMedium/labelLarge.fontSize == 16.sp`, `bodyLarge` and `titleSmall` `17.sp`, `titleMedium` `19.sp`, `titleLarge` `21.sp`.
  - `noReadingStyleUnder16InEitherLook`: for every title, body and label style in `NewtTypography` and in `terminalTypography(NewtTypography)`, assert `fontSize.value >= 16f`.
  - `displayAndHeadlineUnchanged`: assert `NewtTypography.headlineSmall.fontSize == Typography().headlineSmall.fontSize`.
- [ ] **Step 2: Run** `./gradlew :newt-ui:testDebugUnitTest --tests '*TypographyTest*'`. Expected: FAIL (bodySmall is 11.04sp).
- [ ] **Step 3: Implement** by deleting `TEXT_SCALE` and `scaled`, building `NewtTypography`'s title, body and label styles with the existing `reading(sp)` helper, and pointing `NewtReadingTypography` at it. Add the deprecations.
- [ ] **Step 4: Run** the module's tests. Expected: PASS.
- [ ] **Step 5: Commit** "newt-ui: 16sp type floor is the default (TEXT_SCALE removed)".

### Task 2: Outlines, section labels, prompt line, and the leftovers from 1.4.0

**Files:**
- Modify: `newt-ui/.../Components.kt` (`SectionLabel`, `neonOutline`)
- Modify: `newt-ui/.../Look.kt` (`NewtPromptLine` 15.sp → 16.sp)
- Modify: `newt-ui/.../Settings.kt` (`NewtInfo` state wording; `NewtSettingsCard` heading `19.sp` → `MaterialTheme.typography.titleMedium`)
- Modify: `newt-ui/.../Actions.kt` (`NewtStep`: the number circle joins the step's one screen-reader stop and grows with font scale)
- Test: `newt-test/src/test/java/io/github/secnewt/test/PiecesTest.kt`

**Interfaces:**
- Produces: `SectionLabel(text, modifier)` with the same signature that draws `NewtRuleHeading(text, modifier)`; `neonOutline(width: Dp = 2.dp)`.

- [ ] **Step 1: Write the failing tests** in `PiecesTest`:
  - `sectionLabelIsARuleHeading`: `SectionLabel("Today")` has a node with content description "Today" and the heading property, and the text node reads "TODAY" at 16sp or more.
  - `infoStateSaysCollapsedThenExpanded`: `NewtInfo("Longer help")` has stateDescription "Collapsed"; after a click, "Expanded" and the text "Longer help" exists.
  - `stepIsOneStop`: `NewtSteps` with three steps has exactly three merged nodes whose content starts with "Step " (no separate number node).
  - `promptLineIs16sp`: in the terminal look, `assertTextAtLeast(16f)` passes for a `NewtPromptLine`. (The terminal line is hidden from screen readers, so this test rides on Task 3. Write it now and expect it to pass after Task 3.)
- [ ] **Step 2: Run** `./gradlew :newt-test:testDebugUnitTest --tests '*PiecesTest*'`. Expected: the first three FAIL.
- [ ] **Step 3: Implement** the changes listed under Files.
- [ ] **Step 4: Run** the tests again. Expected: the first three PASS.
- [ ] **Step 5: Commit** "newt-ui: 2dp outlines, rule-line section labels, 16sp prompt line".

### Task 3: newtScreenshot checks 16sp by default, and sees all text

**Files:**
- Modify: `newt-test/src/main/java/io/github/secnewt/test/NewtScreenshots.kt`
- Test: `newt-test/src/test/java/io/github/secnewt/test/TextSizeCheckTest.kt`

**Interfaces:**
- Produces: `newtScreenshot(…, minTextSp: Float? = 16f, …)`, where `null` turns the check off for that one test. `assertTextAtLeast(minSp)` also checks text whose semantics were cleared.

- [ ] **Step 1: Write the failing tests:**
  - `defaultChecks16sp`: `newtScreenshot("t-default", dark = true) { Text("tiny", fontSize = 12.sp) }` throws `AssertionError` mentioning "tiny".
  - `nullTurnsItOff`: the same with `minTextSp = null` passes.
  - `seesHiddenText`: `Text("hidden", fontSize = 12.sp, modifier = Modifier.clearAndSetSemantics { })` fails `assertTextAtLeast(16f)`.
- [ ] **Step 2: Run** `./gradlew :newt-test:testDebugUnitTest --tests '*TextSizeCheckTest*'`. Expected: all three FAIL.
- [ ] **Step 3: Implement.** Change the default. To see cleared text, walk the root's `LayoutInfo` tree (`onRoot().fetchSemanticsNode().layoutInfo` and `getChildren()`) and read each `TextLayoutResult` some other way than semantics. If that isn't reliable in Robolectric, keep the semantics walk and make the README and KDoc say exactly what is skipped. Record which one you chose as a ruling.
- [ ] **Step 4: Run** the whole `newt-test` suite. Expected: PASS. Galleries that passed `minTextSp = 16f` explicitly drop the argument.
- [ ] **Step 5: Commit** "newt-test: 16sp check on by default".

### Task 4: Gallery, docs, and the 2.0.0 pull request

**Files:**
- Modify: `README.md` (2.0.0 notes: what grows, how to opt a test out, the migration in three lines)
- Modify: `NEWT_STYLE.md` (replace the "8% smaller" line and the "being rolled out" paragraph with "The bar" from the spec: screens 1–6 and menus M1–M6, one line each)
- Modify: `docs/superpowers/specs/2026-11-04-ui-refresh/` (add `before-2.0.0/` copies of the old gallery images for the PR)

- [ ] **Step 1: Record the screenshots.** Run `./gradlew recordRoborazziDebug` and copy main's gallery images into `before-2.0.0/` for comparison.
- [ ] **Step 2: Run** the full check: `./gradlew testDebugUnitTest lint assembleRelease`. Expected: BUILD SUCCESSFUL.
- [ ] **Step 3: Look at** every changed gallery image at 100% and 200% in light, dark and terminal. Nothing may be clipped, split mid-word or ellipsized.
- [ ] **Step 4: Commit** "Docs: the bar in NEWT_STYLE; 2.0.0 notes". Push branch `ui-refresh-lib-2`, open the PR "UI refresh 2.0.0: 16sp floor, 2dp outlines", link it, and wait for green CI.
- [ ] **Step 5: Wait for "merge it",** then merge and run `gh release create v2.0.0 -R sec-newt/newt-android-lib --title 2.0.0 --generate-notes`. Confirm the publish job succeeds.

---

## After 2.0.0: one plan per app

Each app gets its own short plan, written just before its work starts, in this order (most used first). Each one moves to 2.0.0, fixes whatever the floor overflows, swaps its hand-built rows for the shared pieces, and applies its mockups and its "Feature changes" from the spec:

1. **Newt Phone:** Incoming call (B, with Text back and Silence), Calls/spam (3, Block button), in-call labels, dialpad letters, and the deferred Phone minors (Headphones naming, Block list count, Android-settings line, Watch-mode note on Unknown callers). Drop `NewtReadingText`.
2. **Newt Home:** Launcher (C), Commands help (5, ▶ Try), press-and-hold menu (6), Folders and All apps.
3. **Newt Messages:** Group chat (D), Settings (4), Group details (7, danger behind ⋯), Color picker (8), first-run (12).
4. **Newt Weather:** Main screen (9) and the new Warning alerts setting. Branch `sun-icon` is unfinished; branch from main.
5. **Newt Maps:** Driving (10), Place card (11), first-run (12) with the map-app check.
6. **Newt Reader:** first-run (12), Settings by the rules. Branch `v1-reader` is unfinished; branch from main.
7. **Template:** a sample screen that uses the new pieces.

Every app ships as its own pull request with before/after screenshots. It merges only when the owner says "merge it".
