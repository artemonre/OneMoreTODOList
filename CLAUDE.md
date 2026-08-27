# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Always consult the Android Knowledge Base (android docs) before suggesting any Jetpack API.

## Commands

This is a Kotlin Multiplatform (KMP) project built with Gradle. On Windows use `gradlew.bat` instead of `./gradlew`.

### Build & run
- Android (todo list gateway): `./gradlew :gateway:todoList:assembleDebug`
- Desktop (JVM): `./gradlew :app:desktopApp:run` (or `:app:desktopApp:hotRun --auto` for hot reload)
- Server (Ktor): `./gradlew :server:run`
- Web (WasmJs, faster): `./gradlew :app:webApp:wasmJsBrowserDevelopmentRun`
- Web (JS): `./gradlew :app:webApp:jsBrowserDevelopmentRun`
- iOS: open `app/iosApp` in Xcode (runs the Kotlin framework built from `:app:shared`)

### Tests
- Android: `./gradlew :app:shared:testAndroidHostTest`
- Desktop/JVM: `./gradlew :app:shared:jvmTest`
- Server: `./gradlew :server:test`
- Web: `./gradlew :app:shared:wasmJsTest` / `./gradlew :app:shared:jsTest`
- iOS: `./gradlew :app:shared:iosSimulatorArm64Test`
- Single test: append `--tests "fully.qualified.ClassName.methodName"` to any of the above test tasks.

No detekt/ktlint config and no CI workflows exist in this repo yet.

## Architecture

Gradle modules (declared in `settings.gradle.kts`):

- `core` — pure Kotlin Multiplatform library, no Compose dependency. Shared by every target, including the server. Cross-platform domain/data logic belongs here.
- `app/shared` — Compose Multiplatform UI/logic module, depends on `core` via `api`. Targets Android, iOS (device + simulator), JVM, JS, and WasmJs. Platform-specific code uses `expect`/`actual` in `Platform.<target>.kt` files alongside common code in `commonMain`.
- `app/desktopApp`, `app/webApp` — thin per-platform entry points, each depending on `app:shared` and wiring it into the platform's app shell (desktop `main()`, or web `main()` + `index.html`).
- `app/iosApp` — native SwiftUI Xcode project (not a Gradle module), consumes the Kotlin framework produced by `app:shared`.
- `gateway/<name>` — thin Android **application** entry points ("gateways"). Each is a distinct distributable app (own `namespace`/`applicationId`, app name, app icon under its own `src/main/res`) that depends on `app:shared` plus whichever feature/extra modules make up that product. `gateway/todoList` is the first and currently only one; more are expected as separate products get built from this codebase. Unlike `app/desktopApp`/`app/webApp`, "gateway" is a product-variant axis, not a platform axis — there is intentionally only ever one Android platform target, expressed as N gateway modules.
- `server` — Ktor server application, depends on `core` via `api`. Does **not** depend on `app:shared` — server and client UI are kept separate; only `core` is shared between them.

`build-logic` is a convention-plugin included build (`pluginManagement { includeBuild("build-logic") }` in the root `settings.gradle.kts`). It currently provides one precompiled script plugin, `gateway.application` (`build-logic/convention/src/main/kotlin/gateway.application.gradle.kts`), which every `gateway/*` module applies — it holds everything a gateway module needs *except* its identity (`namespace`, `applicationId`, `versionCode`/`versionName` overrides): SDK versions (from the root version catalog), JVM target, proguard defaults, Compose setup, and the common `app:shared`/activity-compose/Compose-tooling dependencies. A new gateway module's own `build.gradle.kts` should only need `plugins { id("gateway.application") }` plus its `android { namespace = ...; defaultConfig { applicationId = ... } }` block. Non-gateway modules (`core`, `app/shared`, `server`) still apply plugins directly via `alias(libs.plugins.xxx)` — the convention plugin exists specifically to deduplicate the *gateway* modules, not as a repo-wide requirement.

## Git workflow

- New branches are created from `develop` by default, not `master`.
- Feature branches merge into `develop` by default, not `master`. Only merge into `master` when explicitly told to.
- `develop` is the repo's default branch on GitHub (PR base, deletion-protected).
- `git add` (by explicit path, never `-A`/`.`) every new or modified file that belongs in the project as soon as it's created/edited — don't leave it untracked for later, and don't wait to be asked. Skip files that are temp/incidental (build output, IDE crash artifacts, etc.); ask first if it's genuinely unclear whether a file belongs. Staging is not committing — commits and pushes still require an explicit ask each time.
- When told to "commit" without being told what to commit, stage and commit *all* outstanding changes in the working tree (run `git status` first), not just whatever was staged incrementally during the current task — unless the outstanding changes are clearly separate, unrelated work, in which case ask before bundling them in.

## Coding guidelines

- Prefer non-deprecated functions and classes. If the only option is deprecated, or the deprecated one is genuinely the better choice, ask before using it rather than picking silently.
- Avoid hardcoded resources (colors, strings, dimensions, etc.) — create or reuse a shared resource (theme color, string resource, dimension constant, ...) where possible instead of inlining a literal. When planning work and a hardcoded value looks necessary, call it out explicitly in the plan rather than letting it pass silently.
- Treat the app as released once `versionName` (not `versionCode`) moves past `1.0.0` — semantic versioning, major.minor.patch. While `versionName` is still `1.0`/`1.0.0`, Room entities can be edited freely in place (schema version stays at 1, no migration needed). Once `versionName` is past `1.0.0`, never change a Room entity without a migration (bump the `@Database` version and add an `AutoMigration`, or a manual `Migration` when the change is too complex for auto-migration — e.g. column renames/type changes) — and always test the migration.

## Never
- Thread.sleep() → use delay()
- GlobalScope → use viewModelScope
- Broad catch(Exception) → handle specific types
- !! operator → handle nullability explicitly
- Don't run the app or perform manual testing (launching emulators/simulators, clicking through the UI) unless explicitly asked.
- Don't start building/editing when the user asks a question without an explicit call to action — just answer the question. Only move to implementation once they actually ask for a change.
