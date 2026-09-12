plugins {
    id("gateway.application")
    alias(libs.plugins.sentryAndroidGradle)
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
