import com.github.triplet.gradle.androidpublisher.ReleaseStatus
import com.github.triplet.gradle.androidpublisher.ResolutionStrategy

plugins {
    id("gateway.application")
    alias(libs.plugins.sentryAndroidGradle)
    alias(libs.plugins.gradlePlayPublisher)
}

android {
    namespace = "com.artemonre.onemoretodolist"
    defaultConfig {
        applicationId = "com.artemonre.onemoretodolist"
    }
    buildTypes {
        debug {
            resValue("string", "app_name", "OneMoreTODOList-dev")
        }
    }
}

// Uploads the R8/ProGuard mapping file so Sentry can de-obfuscate release crash stack traces -
// without this, a real user's release crash shows mangled class/method names instead of readable
// ones. The plugin reads defaults.org/defaults.project straight from this module's own
// sentry.properties file (see sentry.properties.template) - no extra wiring needed for that part.
// The auth token is intentionally never put in a file - it's read from the SENTRY_AUTH_TOKEN
// environment variable instead, which is also how CI would supply it later.
//
// autoUploadProguardMapping is gated on that env var so a fresh checkout, CI without secrets, or
// this token simply not being set yet still builds successfully (a dry run instead of a real
// upload) - same graceful-degrade shape as every other secret in this module.
sentry {
    autoUploadProguardMapping.set(System.getenv("SENTRY_AUTH_TOKEN") != null)

    // app:shared already brings in the Android Sentry SDK transitively through sentry-kotlin-
    // multiplatform (see libs.sentry.kmp), so the plugin's own auto-added copy just duplicates it
    // under a separate dependency edge. Even though Gradle's version resolution unifies both to
    // the same io.sentry:sentry-android version on paper, the two edges are enough for Sentry's own
    // runtime self-check to trip "Sentry SDK has detected a mix of versions" and crash
    // Application.onCreate() on startup - which is exactly what breaks adding a widget, since
    // Android has to cold-start the process to serve the widget and the crash loop kills it before
    // it can render anything ("Couldn't add widget"). Disabling auto-installation leaves exactly
    // one dependency edge for the Android SDK.
    autoInstallation {
        enabled.set(false)
    }
}

// Uploads the AAB to Play Console via `./gradlew publishBundle` instead of a manual drag-and-drop.
// Credentials come from a local, gitignored service account JSON key file rather than the
// ANDROID_PUBLISHER_CREDENTIALS environment variable - Windows env vars silently truncate a key
// this long (~2.3KB), so the file is the only reliable option locally. Needs a one-time Google
// Cloud service account with Play Console API access before it'll authenticate.
//
// track defaults to "alpha" - the Play Developer API's fixed identifier for the app's current
// Closed testing track (Play Console shows it as "Closed testing - Alpha"; the actual track
// name/title, not "closed" or "closed testing", is what the API expects). A real production
// release needs an explicit `--track production` override, so a bare `publishBundle` can never
// accidentally go live.
// updatePriority is deliberately left unset (GPP defaults it to 0) since it varies per release -
// pass it explicitly at release time instead, e.g. `--update-priority 5` to force the update on
// app startup (blocking); anything below 5 just surfaces the Update button in Settings, left for
// the user to tap whenever they like.
play {
    serviceAccountCredentials.set(rootProject.file("gateway/play-publisher-credentials.json"))
    track.set("alpha")
    releaseStatus.set(ReleaseStatus.COMPLETED)
    defaultToAppBundles.set(true)
    // Replaces the old manual "bump versionCode by 1 on every merge to master" convention -
    // GPP now picks a versionCode higher than whatever's already live on Play at publish time,
    // instead of failing the build when defaultConfig's versionCode has already been used.
    resolutionStrategy.set(ResolutionStrategy.AUTO)
}

dependencies {
    implementation(libs.compose.material3)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    debugImplementation(libs.androidx.glance.preview)
    debugImplementation(libs.androidx.glance.appwidget.preview)
}
