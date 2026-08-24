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

## Never
- Thread.sleep() → use delay()
- GlobalScope → use viewModelScope
- Broad catch(Exception) → handle specific types
- !! operator → handle nullability explicitly
- Don't run the app or perform manual testing (launching emulators/simulators, clicking through the UI) unless explicitly asked.
