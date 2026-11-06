# Newt house style

Shared rules for Newt apps (Newt Phone, Newt Home, and every app made from
newt-android-template), so they look and behave like one family. A new Newt app
should follow this file; later it moves into a shared Newt library.

## Who it's for

The owner has low vision. Readability comes before everything else: large
text, high contrast, big touch targets, and words next to icons.

## Look

- **Colors:** Tokyo Night on true black (`#050608` background) in dark mode,
  Tokyo Night Day in light mode. Every text color is at least **7:1** contrast
  against its background. Accents: blue `#85ABF8`, purple `#BB9AF7`, cyan
  `#7DCFFF`, green `#9ECE6A` (call/go), red `#FF7A93` (end/danger).
- **Outlines:** one thin blue-to-purple gradient outline style for cards,
  buttons and keys. Colors that mean something (a contact's color, green
  Call, red missed or end) keep their own color.
- **Pressed and "on" states:** fill with the same blue-to-purple gradient,
  with dark text on top.
- **Fonts (bundled, never downloaded):** Chakra Petch for screen titles,
  Atkinson Hyperlegible for names and text, JetBrains Mono for numbers.
  No reading text is under 16sp at the phone's default font size; names and row
  titles are 19–21sp. The phone's own font size applies on top.
- **The bar (every screen):**
  1. No text under 16sp, including times, labels, hints and nav labels.
  2. One hero per screen: the biggest thing is what you came for.
  3. Groups sit on a lifted surface with a 2dp border (2.5dp for buttons and inputs).
  4. Never color alone: a colored edge stripe always comes with an icon and a word.
  5. Fill dead space with bigger targets or useful context.
  6. Primary actions sit in the bottom third, 58–84dp tall, always with words. Status goes at the top.
- **The bar (every menu):**
  - M1: the state is a word right by the label (ON/OFF in the switch, or a chip).
  - M2: one line of help at most; anything longer goes behind ⓘ.
  - M3: cards of 3–5 rows, each card with an icon and a heading.
  - M4: a "Right now" sentence at the top of big settings screens.
  - M5: 2–3 options become big segmented buttons; longer lists become pick rows.
  - M6: block, remove and delete live behind ⋯, never inline on every row.
  Design: `docs/superpowers/specs/2026-11-04-ui-refresh-design.md`.
- **Icon:** the solid blue-to-purple newt on near-black, with a colored badge
  for each app (Phone: green handset). Same newt in every app; only the badge
  changes. No glow on the small icon.

## Behavior

- Buttons at least 56dp tall; important ones (Answer, End call) larger, with
  words, not just icons.
- Every screen works at 200% text size and with a screen reader; no two
  things on a screen read out the same.
- Explain settings in one plain sentence; no jargon.
- Private by default: no internet permission unless a feature truly needs
  it, then off by default and explained.
- No Google Play Services, Firebase, analytics or ads.

## How the work is done

- Kotlin, Jetpack Compose and Material 3; built and released on GitHub
  Actions, no local Android setup needed.
- Every pull request runs: logic tests, screenshots in light, dark and 200%
  text, accessibility checks, lint, a permission allowlist check, a secret scan
  and, where it applies, an emulator test.
- Changes merge only after the owner says "merge it". Merges to `main`
  publish a signed release; installs and updates go through Obtainium.
- Updates to the owner are short, plain language, and say what to try on
  the phone.
