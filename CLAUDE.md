# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Always consult the Android Knowledge Base (android docs) before suggesting any Jetpack API.

@../claude_conventions.md

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
