# Newt UI refresh: design

Date: 2026-11-04 · Status: approved for the library pieces (1.4.0) and a Newt Phone test build (Settings + Recents); everything else open until the phone test

Mockups (open in a browser): [round 1, main screens](2026-11-04-ui-refresh/round1-screens.html),
[round 2, menus and the rest](2026-11-04-ui-refresh/round2-menus.html). Each one shows the
current screenshot ("NOW") next to the proposed screen. **The mockups are the visual
reference for this document.** Where they and this text disagree, this text wins.

## Why

The owner has low vision. The Newt apps already have real contrast, words next to icons and
big main buttons, but they score about 6.5/10 for him. The goal is 9–10/10 on every screen
while keeping the low-vision focus. What's wrong today:

- **Secondary text is too small.** `NewtTypography` scales body and label text to 92% of
  Material's sizes (`TEXT_SCALE = 0.92f`), so times, "Missed", sender names and setting
  descriptions come out around 11–12sp.
- **Menus read as walls of text.** Every setting has a paragraph under it. The current
  value is small text far from its label ("Announce ……… Off"). Switches have no words.
  Three-way choices are radio lists where every option has a description. Red
  "Block / Remove" links repeat on every row.
- **Thin 1.5dp outlines on black disappear** at low vision, so groups don't read as groups.
- **Empty space isn't used.** The incoming call, Maps and Reader first-run screens, and
  driving all leave half the screen empty while their buttons stay small.
- **Some things are shown by color alone,** like the sender's color in group chats.

## Goals

1. Every screen in every Newt app reaches the bar below, in light, dark and terminal looks,
   at 100% and 200% text.
2. The shared pieces live in `newt-ui`, so all apps look and behave the same and future
   apps get it for free. Apps should stop building their own settings rows.
3. The `NEWT_STYLE.md` rules are updated so the bar is written down.

## Non-goals

- No new color palette, fonts or icon. Tokyo Night, the three bundled fonts and the newt stay.
- No change to the terminal / clean family look mechanism (`NewtLookTheme`). Both looks get
  the new pieces.
- No new permissions, internet use or dependencies.
- This is not a rewrite of the apps' logic. Behavior changes are limited to the list in
  "Feature changes" below.

## Anti-goals (a failure even if it ships)

- Anything that's harder to read at 200% text than today. Overflowing or clipped text
  counts as a failure.
- Losing screen-reader quality: two things reading out the same, or a switch whose state
  isn't announced.
- Hiding information the owner needs to make a safe choice. Permission and privacy
  explanations move behind ⓘ, but they must still show **when he turns the thing on**.
- Mis-tap risk going up, for example destructive actions placed next to common ones.

## The bar (rules every screen must meet)

These go into `NEWT_STYLE.md`, replacing the "8% smaller" line.

Screens:

1. **No text under 16sp** at the phone's default font size, including times, labels,
   hints and nav labels. Names and row titles are 19–21sp.
2. **One hero per screen.** The biggest thing is what you came for: the caller, the clock,
   the next turn, the current temperature.
3. **Surfaces, not just outlines.** Groups sit on a lifted surface (`surfaceContainerLow`)
   with a **2dp** border (2.5dp for buttons and inputs).
4. **Never color alone.** Meaningful color always comes with an icon and a word: a 6dp
   colored edge stripe plus "⚠ Likely spam", or a sender's color stripe plus their name. (A
   color's *name*, like "Yellow", is written where the color is chosen or managed, such as Group
   details and the color picker, not on every message.)
5. **Fill dead space** with bigger targets or useful context.
6. **Thumb zone = action zone.** Primary actions sit in the bottom third, 58–84dp tall,
   always with words. Status goes at the top.

Menus:

- **M1 The answer comes first:** the state is a word right by the label (ON/OFF inside the
  switch, or a chip such as "🔕 Silence" or "Balanced").
- **M2 One line of help, max,** at 16sp. Anything longer goes behind ⓘ.
- **M3 Cards of 3–5 rows,** each with an icon and a heading.
- **M4 A "Right now" summary** at the top of big settings screens: one plain sentence about
  what the phone is doing.
- **M5 Choices of 2–3 options become big segmented buttons** (54dp). Longer lists become
  pick rows with a large check, and only the chosen row shows its hint.
- **M6 Danger behind ⋯.** Block, remove and delete live in a row's ⋯ menu, not inline on
  every row.

## Library work (`newt-ui`)

All new pieces work in light, dark, terminal and clean looks, take text as non-null where
a word is required, and have their own screen-reader semantics. They're all pictured in
`GalleryTest` (light, dark, 200%, terminal).

### Changes to what exists

| What | Now | Change |
|---|---|---|
| `TEXT_SCALE` | 0.92 | Remove (1.0). Add a floor: `bodySmall`, `labelSmall`, `labelMedium`, `bodyMedium` → **16sp**; `bodyLarge` 17sp; `titleMedium` 19sp; `titleLarge` 21sp. The terminal look's ×0.94 mono shrink still applies, but clamps at **16sp** (the floor holds in the terminal look too). `NewtPromptLine`'s hard-coded 15sp becomes 16sp. |
| `neonOutline()` default width | 1.5dp | 2dp. |
| `SectionLabel` | small label | 16sp minimum, mono, letter-spaced, with a rule line after it (the "TODAY ———" look in mockup A). |

### New pieces

| Piece | What it is | Used in mockups |
|---|---|---|
| `NewtSwitch` | A switch with **ON/OFF written inside**: 74×40dp visual, 48dp+ touch target, gradient when on, outlined when off. Semantics: `Role.Switch` plus state, so it reads "Favorites, on, switch" once. | 1, 2, 4, 9 |
| `NewtSettingsCard(icon, title)` | A lifted card with a heading in cyan Chakra Petch. Rows go inside it. | 1, 2, 4 |
| `NewtSettingRow` | The row layout everything shares: title 19sp bold, an optional one-line hint 16sp, and a trailing slot. Variants: **switch row**, **value row** (chip plus › that opens a screen), **locked row** ("🔒 Always", not tappable, explained in its semantics). The whole row is the touch target. | 1, 2, 4, 9 |
| `NewtStateChip(text, icon, tone)` | A word chip with tones Danger (red), Good (green), Info (blue) and Neutral. It always has text. | 2, 3, 1 |
| `NewtChoice` | A segmented control for 2–3 options, 54dp tall, gradient on the selected one, with an optional one-line explanation of the selected option below. Semantics: radio group. | 1, 2, 4, 8 |
| `NewtPickList` | Pick rows for 4+ options (or for options with hints): a 30dp check circle, and only the selected row shows its hint. | 4 |
| `NewtSummary(label, text, tone)` | The "RIGHT NOW" box: an edge stripe, a small mono label and one sentence. | 1, 3 |
| `Modifier.newtStripe(color)` | A 6dp leading edge stripe plus a faint tint, used for spam/missed rows, alerts and sender colors. Decoration only; the meaning must also be in the text. | A, 3, 7, 9, D |
| `NewtButton` / `NewtPrimaryButton` | Worded buttons 58dp tall (64dp `large`), outlined or filled. `text` is required. `NewtPrimaryButton` takes a tone (Go = green, Danger = red outline). | most |
| `NewtActionGrid` | A 2-column grid of action tiles (label plus optional one-line hint) for bottom sheets. | 6 |
| `NewtSteps` | Numbered first-run steps with done ✓ (green), now (lit, holds its button) and to-do states. | 12 |
| `NewtInfo` | A ⓘ "What's this?" line that expands longer help in place. | 2 |

## Per-app work

Each app moves to the new library version, swaps its hand-built rows for the shared
pieces, and applies the screen changes from the mockups. Screens that aren't mocked get the
same treatment by following the rules (for example the Block list, ZIP screen, Contact
page, Folders, and Reader settings).

| App | Screens (mockup) |
|---|---|
| Newt Phone | Recents (A), Incoming call (B), Settings (1), Unknown callers (2), Calls/spam (3). Light touch: dialpad letters 16sp, matching-contact rows 21sp; in-call buttons get 16sp labels and are filled while on ("Muted"). |
| Newt Home | Launcher (C), Commands help (5), press-and-hold menu (6). Folders and All apps get the 2dp borders and lifted cards. Leave the letter grid as it is. |
| Newt Messages | Group chat (D), Settings (4), Group details (7), Color picker (8), first-run (12). |
| Newt Weather | Main screen (9): alerts as stripe rows, warning notifications as one switch row, 7-day forecast on the first screen. |
| Newt Maps | Driving (10), Place card (11), first-run (12). |
| Newt Reader | First-run (12), Settings by the rules. |
| Template | Uses the new pieces in its sample screen, so new apps start right. |

### Feature changes (the only behavior changes allowed)

These go beyond looks. The owner approved them with the mockups.

- **Phone, incoming call:** context lines (★ favorite / number type, "last talked …"),
  **Text back** (declines the call, the same as Android's own "reply with message", then opens a
  reply to this number in the default SMS app; no new permission)
  and **Silence** (stops the ringer and keeps ringing the call silently). Answer
  becomes the largest button, and Decline is separate from it, not side by side.
- **Phone, settings:** the "Observe only" switch is shown as a **Watch / Block** choice.
  It's the same stored setting, just presented the other way round.
- **Phone, Calls:** a **Block** button on each screened call (it already exists as a link).
- **Home, launcher:** "NEXT · IN 45 MIN" countdown on the event card, weather on the date
  line, and favorites as 2-column tiles with a "2 missed" pill.
- **Home, commands help:** commands grouped by topic, tap a command to show one line and
  **▶ Try**, which puts the command in the command box with its usual preview. He presses
  Enter to run it, so a mis-tap never sets an alarm or flips a setting by itself. The mockup's `text becca` command doesn't exist and
  isn't part of this work.
- **Weather, warning alerts:** a new in-app **Warning alerts** setting behind the switch
  (the owner's choice). ON asks for notification permission if needed, with the
  explanation shown at that moment. OFF stops warning notifications while the permission
  stays granted. The switch always flips and announces its state. Default: ON if the
  permission is already granted, otherwise OFF.
- **Maps, first-run:** step 1 checks whether a map app is installed and shows "<app> found"
  or what to install.
- **Messages, group details:** Block, Remove and Change color move into the ⋯ menu.

## How we know it worked

- Every app's existing screenshot and accessibility checks pass in light, dark and 200%
  text (and terminal where the app has it). Screenshots change on purpose, and each app's
  pull request shows before/after.
- New checks in `newt-test`: **no text under 16sp** at 100% (`newtScreenshot` fails it) and
  **touch targets ≥ 48dp** (already Android's own check; keep it on).
- `GalleryTest` pictures every new piece.
- The owner tries each app on his phone and is shown what to try. An app's change merges
  only when he says "merge it".

## Order of work

1. **Library, additive:** the new pieces plus the gallery. No visual change to existing
   apps. Release **1.4.0**.
2. **Library, the look change:** type floor, 2dp outlines, `SectionLabel`, the 16sp check
   in `newt-test`, and `NEWT_STYLE.md`. Release **2.0.0**, because apps may have to fix
   layouts that overflow with bigger text.
3. **Apps, one at a time,** most-used first: Phone → Home → Messages → Weather → Maps →
   Reader → template. Each app is one pull request (or a short series) that moves to 2.0.0
   and applies its mockups.

## Calls I made (the owner can overrule)

- Splitting the library into 1.4.0 (additive) and 2.0.0 (the visual change), so the new
  pieces can be reviewed in the gallery before every app's text gets bigger.
- The exact type sizes in the table above, chosen to match the mockups.
- **Text back** declines the call (as Android's own dialer does) and uses the default SMS
  app's reply intent rather than a built-in quick-reply list, so it needs no new permission.
- **▶ Try** fills the command box rather than running the command straight away (mis-tap safety).
- The 16sp floor applies in the terminal look too. Color names aren't repeated on every chat
  message.
- **Silence** on an incoming call keeps the call ringing silently rather than declining it.
- The "no text under 16sp" check measures font size in sp at the default font scale and
  skips decorative text (the gadget-background newt is an image already).
- Mockups use emoji as stand-ins. The real screens use Material icons.

## Left to the builder

The internal API shape of each component (parameter names, slot vs. enum variants), how
the 16sp check is written in `newt-test`, and exact spacing within the ranges the mockups
show.
