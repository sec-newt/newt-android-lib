# What to look for when reviewing the Newt Android library

Read [CLAUDE.md](CLAUDE.md) first; flag code that breaks a rule there, and lines in it
that have gone stale.

## What "broken" means here

In order of how bad it is:

1. **It weakens every app's CI.** A change to `newt-app.yml` that loosens permissions,
   unpins an action, exposes the signing key or secrets, or lets PR code run with them.
2. **It breaks every app's readability.** A shared piece under 16sp or 7:1, smaller touch
   targets, missing TalkBack labels, a layout that breaks at 200% text, or a `newt-test`
   check made weaker so it lets those through.
3. **It breaks apps on update.** A removed or renamed piece, or changed behavior, without
   a major version bump and a README note.
4. **It lies.** README, a label, a hint or a status that says something different from
   what the code does.

## Always call out, even if it looks fine

Any change to `.github/workflows/`, `scripts/`, `config/allowed-permissions.txt`,
`AndroidManifest.xml`, signing, release, or `gradle/libs.versions.toml`. Especially `newt-app.yml`: every app runs it. Say in
one line what changed so the owner reads it themselves.

## How to report

- Evidence for every finding: file and line, and what goes wrong for the person using it.
  Don't guess from a name.
- Tag each one **Important**, **Nit** or **Pre-existing** (a problem this PR didn't add).
- At most 3 nits. Skip style, naming and formatting unless it hurts readability.
- Start the summary with a count: "2 important, 1 nit." If nothing needs fixing, say so
  plainly: "Nothing to fix."
- Don't repeat a finding that's already posted on this PR.

## Skip

`app/build/`, `build/`, `docs/superpowers/` (old plans and specs), and screenshot images.
