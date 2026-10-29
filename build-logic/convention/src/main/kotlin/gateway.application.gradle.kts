import com.android.build.api.variant.BuildConfigField
import java.util.Properties
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

// Each gateway module has its own upload key, so this reads keystore.properties from that
// module's own directory (not the repo root) - see keystore.properties.template. Never committed;
// a module without one (a fresh checkout, CI without secrets) just builds an unsigned release.
val keystorePropertiesFile = project.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

// Same convention as keystore.properties above: gitignored, per-module, see
// sentry.properties.template. Missing file (a fresh checkout, CI without secrets) just means an
// empty DSN, which Observability.kt's initSentry() treats as "disabled".
val sentryPropertiesFile = project.file("sentry.properties")
val sentryProperties = Properties().apply {
    if (sentryPropertiesFile.exists()) {
        sentryPropertiesFile.inputStream().use { load(it) }
    }
}

// Same convention again, see posthog.properties.template. Missing file means an empty API key,
// which Analytics.kt's initPostHog() treats as "disabled".
val posthogPropertiesFile = project.file("posthog.properties")
val posthogProperties = Properties().apply {
    if (posthogPropertiesFile.exists()) {
        posthogPropertiesFile.inputStream().use { load(it) }
    }
}

android {
    compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()


    defaultConfig {
        minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()
        targetSdk = libs.findVersion("android-targetSdk").get().requiredVersion.toInt()
        versionCode = 7
        versionName = "1.3.2"
    }

    androidResources {
        localeFilters += listOf("ru", "sr")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    if (keystorePropertiesFile.exists()) {
        signingConfigs {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }
    buildTypes {
        debug {
            // Lets a debug build sit side by side with a release install of the same app instead
            // of overwriting it.
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"

        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        // Lets a gateway module override resources like app_name per build type (e.g. a "-dev"
        // app name suffix for debug) via resValue().
        resValues = true
        buildConfig = true
    }

//    applicationVariants.all {
//        this.outputs
//            .map { it as com.android.build.gradle.internal.api.ApkVariantOutputImpl }
//            .forEach { output ->
//                val variant = this.buildType.name.uppercaseFirstChar()
//                val flavor = this.flavorName
//                val apkName = "todoList-$appVersionName-$appVersionCode-$flavor$variant.apk"
//                output.outputFileName = apkName
//            }
//    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

// versionCode isn't settable per buildType in the DSL block above (only defaultConfig/flavors
// expose it), so debug's override goes through the variant API instead. Pinned rather than
// following defaultConfig's - debug builds are never uploaded anywhere, so there's nothing for it
// to track.
androidComponents {
    // The real keys go into the release build only. Every other build type - debug, and the
    // Baseline Profile plugin's copies of release (nonMinifiedRelease, benchmarkRelease) - gets
    // empty ones, which initSentry()/initPostHog() treat as "disabled". Same fields everywhere, so
    // the same code compiles in every variant; only what's in them differs. Each gateway starts
    // Sentry/PostHog from its own initObservability(): the real one in src/release, a no-op in
    // src/debug (see gateway/todoList).
    onVariants { variant ->
        val isRelease = variant.buildType == "release"
        val sentryDsn = if (isRelease) sentryProperties.getProperty("dsn", "") else ""
        val posthogApiKey = if (isRelease) posthogProperties.getProperty("apiKey", "") else ""
        variant.buildConfigFields?.put("SENTRY_DSN", BuildConfigField("String", "\"$sentryDsn\"", null))
        variant.buildConfigFields?.put("POSTHOG_API_KEY", BuildConfigField("String", "\"$posthogApiKey\"", null))
    }
    onVariants(selector().withBuildType("debug")) { variant ->
        variant.outputs.forEach { output ->
            output.versionCode.set(1)
        }
    }
}

dependencies {
    implementation(project(":app:shared"))
    implementation(libs.findLibrary("androidx-activity-compose").get())
    implementation(libs.findLibrary("compose-uiToolingPreview").get())
    debugImplementation(libs.findLibrary("compose-uiTooling").get())
}
