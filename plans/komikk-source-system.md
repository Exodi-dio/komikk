# komikk — Source/Extension System (Milestone 1)

**Objective:** Make komikk the better version of every Tachiyomi fork by building
its foundation: a first-class, testable source/extension system.

**Decisions locked in kickoff:**
- New public repo `Exodi-dio/komikk` (existing `komikku` fork stays untouched).
- Android-native, Kotlin + Compose (KMP, Android-first — mirror the Airmedy mobile policy).
- Start with the source/extension architecture, not the reader.

**Why "single-process Source SPI + registry" beats the forks:**
- Tachiyomi/Mihon lineage keeps each source as a *separate APK* bound over IPC
  (side-install friction, per-extension permission surface, host/extension
  version-matching drift, source crash = process death).
- komikk v1 delivers sources as **in-repo language plugins compiled into the
  app**, discovered through a **statically generated registry** (KSP), not
  runtime reflection or IPC. Deterministic, unit-testable, crash-contained.
- The SPI stays stable so a later step can externalize plugins (downloadable
  plugin APKs / catalog channel / dynamic delivery) without touching core models.

## Module graph

```text
core/            KMP: models, Source SPI, @KomikkSource, Paged, errors, prefs contract (NO Android imports)
plugins/         Android/KMP libs implementing the SPI
  sample/        fixture-driven plugin (search/latest/chapters/pages from test data)
  mangadex/      real plugin against the public MangaDex API
  manganato/     HTML-scraping plugin (Proofs DOM parsing + per-page headers) [roadmap]
androidApp/      Compose shell: SourceManager, Catalog, SourceDetail, chapter list, reader stub
registry (KSP)   generates SourceRegistry from @KomikkSource annotations
```

## SPI contract (core)

```kotlin
@KomikkSource(id = "...", name = "...", lang = "en", version = 1)
interface Source {                     // every plugin provides exactly one of these

    suspend fun getPopular(page: Int): Paged<Manga>
    suspend fun getLatest(page: Int): Paged<Manga>?   // null when unsupported
    suspend fun search(query: String, page: Int): Paged<Manga>
    suspend fun getMangaDetails(manga: Manga): Manga
    suspend fun getChapters(manga: Manga): List<Chapter>
    suspend fun getPages(chapter: Chapter): List<Page>

    fun preferences(): SourcePreferences      // per-source settings (UA, thumbs, etc.)
}
```

- `Manga`/`Chapter`/`Page` are pure KMP value types. URLs stay *lazy* (resolved by
  `getPages`) so sources control per-request headers (Referer, cookies).
- `Paged<T>` carries items + hasNext so the UI can drive infinite scroll;
  failures are signaled by throwing `SourceException` (sealed mode model:
  Network/Unauthorized/NotFound/RateLimited/Parse/FeatureNotSupported), never
  carried inside the page result.
- Network goes through a `SourceHttpExecutor` interface defined in `core` and
  implemented once in `androidApp` (OkHttp): per-source User-Agent/cookie-jar
  isolation, retry/backoff, and request-tagging for cancellation. Tests use
  MockWebServer against this interface.
- Identity rules: source `id` is a stable reverse-DNS string; plugin `version`
  bumps on contract change. These are unit-tested invariants.

## Registry

KSP processor `@KomikkSource` -> `GeneratedSourceRegistry` listing each
instantiable source. `SourceManager` (androidApp) wraps it: instantiation,
injectable dependencies, per-source state, and a settings store keyed by
source id. Fallback if KSP adds friction: a hand-written source list in
`androidApp` behind the same `SourceManager` API.

## Steps (each = one PR, baseline stays green after every one)

**Step 1 — Repo scaffold** *(no deps)*
Context: empty repo. Stand up the KMP Android app (Compose shell, minimal theme),
`AGENTS.md`, `README.md`, `plans/`, baseline CI (compile + core tests +
assembleDebug), Gradle version catalog.
Tasks: monorepo layout from module graph; compose shell with an empty Catalog
screen; CI workflow; verify `./gradlew :androidApp:assembleDebug` + `:core:test`.
Exit: repo builds green in CI from a cold checkout; app launches, shows shell.

**Step 2 — core: models + SPI + contracts** *(dep: 1)*
Context: `core/` exists empty. Implement value types (Manga/Chapter/Page),
`Paged`, error model, the five SPI methods as above, `@KomikkSource` annotation,
`SourceHttpExecutor`, `SourcePreferences` contract. No Android imports allowed.
Tasks: commonTest invariants (stable ids, no-Android types in models, paging
edges); keep `core` free of Android deps in its Gradle file.
Exit: `:core:test` green; architecture check (no `android` deps) passes.

**Step 3 — registry + SourceManager + wiring** *(dep: 2)*
Context: SPI and models exist and are tested. Generate the registry from
annotations (KSP), add `SourceManager` in `androidApp`, skim Catalog screen to
list discovered sources with live state.
Tasks: processor + tests (classpath scanning fixture); SourceManager unit tests
with fakes; Ui smoke test asserting Catalog renders sources from a stub registry.
Exit: `assembleDebug` + host tests green; Catalog lists registered sources.

**Step 4 — sample + MangaDex plugins** *(dep: 2, 3)*
Context: registry can load plugins; now prove real sources against it.
sample-plugin ships fixture data; mangadex-plugin uses the public (auth-free)
MangaDex API with offset paging, rate-limit retry, and error mapping.
Tasks: implement both plugins; MockWebServer tests covering paging wrap,
404/429 -> `SourceException`, empty search; add plugin screens smoke test.
Parallel: sample and mangadex can land as two merges in either order after 3.
Exit: encouraging a hard-coded source through search -> chapters -> pages
end-to-end in unit tests; same path works from Catalog UI.

**Step 5 — catalog depth + reader stub** *(dep: 4)*
Context: sources are real; make the app worth opening. Source detail (browse
latest, search with paging + loading/error/empty states), chapter list, and a
paged reader stub that walks the resolved pages.
Tasks: Compose screens + UI tests for state transitions; wire SourceManager
state (loading/error) through Compose; hook Coil for page images behind the
SourceHttpExecutor headers.
Exit: `assembleDebug` + host tests green; crawl a source UI path end-to-end.

**Step 6 — docs + first release** *(dep: 5)*
Context: architecture proven. Write `catalog/README.md` (SPI contract,
SourceManager, adding-a-source guide), polish CI, add a v0.1 debug release
workflow (public-repo free tier only).
Exit: release APK artifact exists; new-agent-after-three-months can add a source.

## Roadmap gates (NOT in this milestone — do not build yet)
- Externalize plugins: in-app download/update channel (plugin APKs or dynamic
  delivery) reusing the same SPI.
- Reader v1 (webtoon/manga pager, progress, shims).
- Multi-device sync between komikk installs.

## Invariants (re-verified after every step)
- All prior tests remain green; `:core` never imports Android.
- Source ids unique and stable across builds (`GeneratedSourceRegistry`).
- No emulator/device verification: host Gradle tasks only (repo policy).
- Repo stays public; Actions stays on the free tier, no billing-dependent jobs.

## Rollback
Every step lands as a squash-merge. Red is fixed with a follow-up commit on a
new branch, never an amend of a merged commit. Last-known-green is the merge
before the failing PR.