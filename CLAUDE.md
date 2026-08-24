# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Always consult the Android Knowledge Base (android docs) before suggesting any Jetpack API.

## Commands

This is a Kotlin Multiplatform (KMP) project built with Gradle. On Windows use `gradlew.bat` instead of `./gradlew`.

### Build & run
- Android: `./gradlew :app:androidApp:assembleDebug`
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
- `app/androidApp`, `app/desktopApp`, `app/webApp` — thin per-platform entry points, each depending on `app:shared` and wiring it into the platform's app shell (Android `Activity`, desktop `main()`, or web `main()` + `index.html`).
- `app/iosApp` — native SwiftUI Xcode project (not a Gradle module), consumes the Kotlin framework produced by `app:shared`.
- `server` — Ktor server application, depends on `core` via `api`. Does **not** depend on `app:shared` — server and client UI are kept separate; only `core` is shared between them.

There is no `build-logic`/convention-plugin module: every module applies Kotlin/Android/Compose/Ktor plugins directly via `alias(libs.plugins.xxx)` in its own `build.gradle.kts`, using the version catalog at `gradle/libs.versions.toml`.

## Git workflow

- New branches are created from `develop` by default, not `master`.
- Feature branches merge into `develop` by default, not `master`. Only merge into `master` when explicitly told to.
- `develop` is the repo's default branch on GitHub (PR base, deletion-protected).
- `git add` (by explicit path, never `-A`/`.`) every new or modified file that belongs in the project as soon as it's created/edited — don't leave it untracked for later, and don't wait to be asked. Skip files that are temp/incidental (build output, IDE crash artifacts, etc.); ask first if it's genuinely unclear whether a file belongs. Staging is not committing — commits and pushes still require an explicit ask each time.
- When told to "commit" without being told what to commit, stage and commit *all* outstanding changes in the working tree (run `git status` first), not just whatever was staged incrementally during the current task — unless the outstanding changes are clearly separate, unrelated work, in which case ask before bundling them in.

## Coding guidelines

- Prefer non-deprecated functions and classes. If the only option is deprecated, or the deprecated one is genuinely the better choice, ask before using it rather than picking silently.
- Avoid hardcoded resources (colors, strings, dimensions, etc.) — create or reuse a shared resource (theme color, string resource, dimension constant, ...) where possible instead of inlining a literal. When planning work and a hardcoded value looks necessary, call it out explicitly in the plan rather than letting it pass silently.

## Never
- Thread.sleep() → use delay()
- GlobalScope → use viewModelScope
- Broad catch(Exception) → handle specific types
- !! operator → handle nullability explicitly
- Don't run the app or perform manual testing (launching emulators/simulators, clicking through the UI) unless explicitly asked.
