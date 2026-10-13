# Security

## Reporting a problem

Please report security problems **privately**: on this repository, open the
**Security** tab and choose **Report a vulnerability**. Please don't open a
public issue for them.

This is a one-person hobby project, so replies may take a few days.

## What this library may do

It holds look-and-feel code only: colors, fonts, text styles and shared screen pieces,
plus test helpers that never ship inside an app. It asks for no permissions and has no
network code; each app's permission check would catch it if that changed.

## How releases are protected

- Versions are published to GitHub Packages by GitHub Actions, only from a
  GitHub release and only after every check passes. Published versions can't be
  replaced, and release tags are protected from being moved or deleted.
- Every change passes tests, Android lint, the permission check and a scan for
  leaked passwords or keys before it can merge.
- Build tools are pinned to exact versions, and Dependabot proposes updates,
  which go through the same checks.
