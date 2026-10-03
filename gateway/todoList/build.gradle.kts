import com.github.triplet.gradle.androidpublisher.ReleaseStatus
import com.github.triplet.gradle.androidpublisher.ResolutionStrategy

plugins {
    id("gateway.application")
    alias(libs.plugins.ksp)
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
    // Local JVM unit tests (src/test) cover plain Kotlin logic only - anything that reaches into
    // the Android framework stubs gets a default value instead of a "not mocked" crash.
    testOptions {
        unitTests.isReturnDefaultValues = true
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

// Release guardrails - every task that uploads a build to Play (publish*Bundle/Apk/Apps,
// promote*Artifact) first runs verifyReleaseReady, which fails unless:
// - the current branch is release/v<versionName> and has no uncommitted changes to tracked files
//   (a release is always cut on its own branch, never straight from develop or master)
// - no v<versionName> tag exists yet (catches a forgotten versionName bump)
// - release-notes/v<versionName>.md exists and carries an "Approved: yes" line - added only after
//   the notes were reviewed and accepted - and its "## Play notes" section matches alpha.txt
//   exactly and fits Play's 500-character limit
// Listing/products/subscriptions tasks aren't tied to a build version, so they're not gated.
val verifyReleaseReady = tasks.register("verifyReleaseReady") {
    group = "publishing"
    description = "Fails unless this checkout is in a state that's allowed to publish to Play."
    // Locals rather than script-level vals - the configuration cache can't serialize a doLast that
    // reaches back into the build script object.
    val releaseVersionName = android.defaultConfig.versionName.orEmpty()
    val releaseBranch = providers.exec { commandLine("git", "rev-parse", "--abbrev-ref", "HEAD") }
        .standardOutput.asText.map { it.trim() }
    val trackedChanges = providers.exec { commandLine("git", "status", "--porcelain", "--untracked-files=no") }
        .standardOutput.asText.map { it.trim() }
    val existingReleaseTag = providers.exec { commandLine("git", "tag", "--list", "v$releaseVersionName") }
        .standardOutput.asText.map { it.trim() }
    val releaseNotesDraft = rootProject.layout.projectDirectory.file("release-notes/v$releaseVersionName.md")
    val playReleaseNotes = layout.projectDirectory.file("src/main/play/release-notes/en-US/alpha.txt")
    doLast {
        val problems = buildList {
            val expectedBranch = "release/v$releaseVersionName"
            if (releaseBranch.get() != expectedBranch) {
                add("Publish only from $expectedBranch - current branch is ${releaseBranch.get()}.")
            }
            if (trackedChanges.get().isNotEmpty()) {
                add("Commit or discard tracked changes first:\n${trackedChanges.get()}")
            }
            if (existingReleaseTag.get().isNotEmpty()) {
                add("Tag v$releaseVersionName already exists - bump versionName for a new release.")
            }
            val draftFile = releaseNotesDraft.asFile
            if (!draftFile.isFile) {
                add("Missing ${draftFile.path} - draft the release notes and get them reviewed first.")
            } else {
                val draft = draftFile.readText()
                if (draft.lines().none { it.trim().equals("Approved: yes", ignoreCase = true) }) {
                    add("Release notes v$releaseVersionName aren't approved yet (no \"Approved: yes\" line).")
                }
                val approvedPlayNotes = draft.substringAfter("## Play notes", missingDelimiterValue = "")
                    .substringAfter('\n').trim()
                val publishedPlayNotes = playReleaseNotes.asFile.readText().trim()
                if (approvedPlayNotes != publishedPlayNotes) {
                    add("alpha.txt doesn't match the approved \"## Play notes\" section of ${draftFile.name}.")
                }
                if (publishedPlayNotes.length > 500) {
                    add("alpha.txt is ${publishedPlayNotes.length} characters - Play allows 500.")
                }
            }
        }
        if (problems.isNotEmpty()) {
            throw GradleException("Not ready to publish:\n- " + problems.joinToString("\n- "))
        }
    }
}

tasks.matching { task ->
    listOf("Bundle", "Apk", "Apps").any { task.name.startsWith("publish") && task.name.endsWith(it) } ||
        (task.name.startsWith("promote") && task.name.endsWith("Artifact"))
}.configureEach { dependsOn(verifyReleaseReady) }

dependencies {
    implementation(libs.compose.material3)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.androidx.appfunctions)
    ksp(libs.androidx.appfunctions.compiler)
    testImplementation(libs.kotlin.testJunit)
    debugImplementation(libs.androidx.glance.preview)
    debugImplementation(libs.androidx.glance.appwidget.preview)
}

// AppFunctions (on-device agent access, see appfunctions/BaseTodoAppFunctionService.kt): the
// compiler generates the concrete service plus the XML that describes every function to the OS.
// Aggregation is on for this module since it's the app that ships the service.
ksp {
    arg("appfunctions:aggregateAppFunctions", "true")
}
