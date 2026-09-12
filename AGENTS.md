# komikk Agent Guide

## Mandate

komikk is a from-scratch Android reader whose differentiator is a clean,
testable source/extension system. Read `plans/komikk-source-system.md` before
changing anything — it is the living construction plan for the first milestone.

## Repository policy

- This repo is **public** by default. Keep GitHub Actions on the public free
  tier; no billing/payment-dependent jobs.
- Repository is Android-first Kotlin Multiplatform. Do not add iOS targets,
  Compose Multiplatform UI, or desktop work unless a task explicitly asks.

## Module boundaries

```text
core/            models + Source SPI + annotations + prefs contract (NO Android imports)
plugins/         in-process source plugins implementing the SPI (sample, mangadex, ...)
androidApp/      Compose UI + SourceManager + adapter implementations (OkHttp, storage)
registry (KSP)   generates SourceRegistry from @KomikkSource annotations
```

- Dependency direction is inward: `androidApp` depends on `core` and plugins;
  `core` depends on nothing but its own contracts and multiplatform libraries.
- `core` must not import Android, Compose, or UI state APIs. Keep it
  platform-neutral and unit-testable (`kotlin.test`).
- Plugin sources talk to the network only through `SourceHttpExecutor` defined
  in `core` and implemented in `androidApp`. Never open raw HTTP clients inside
  a plugin.
- Source identifiers are stable reverse-DNS strings; uniqueness and stability
  are unit-tested invariants. Never reuse or mutate a source id.

## Conventions

- Use the Gradle version catalog (`gradle/libs.versions.toml`); never hardcode
  dependency versions in module build files.
- Android SDK policy: `minSdk 26`, `compileSdk 36`, `targetSdk 36`
  (adjust once in Step 1 if reality differs; keep minSdk low — this is a
  low-end-device project).
- Strings, icons, themes, and accessibility semantics live in Android
  resources or Android UI code. Keep UI strings out of `core`.
- Keep mutable UI state in ViewModels. Collect flows lifecycle-aware; release
  listeners/jobs when lifecycles end.

## Verification

Host Gradle tasks only — never run on a device or emulator unless explicitly
authorized.

```bash
./gradlew :core:test                     # SPI + model contracts
./gradlew :androidApp:assembleDebug      # app builds
```

Baseline must stay green after every PR. Every new feature or fix ships with a
test (contract tests in `core`, MockWebServer-based plugin tests, Compose UI
tests for screen states).

## Out of scope until explicitly requested

- iOS, desktop, sync accounts, reader polish beyond the milestone stub.
- Plugin download/update channel (externalized sources) — it is a roadmap
  gate in the plan; do not build it while Milestone 1 is open.
- Anything that requires paid/funded infra on GitHub Actions.