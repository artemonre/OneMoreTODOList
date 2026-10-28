import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidTest)
    alias(libs.plugins.androidxBaselineprofile)
}

// Generates the Baseline Profile for :gateway:todoList - `./gradlew :gateway:todoList:generateBaselineProfile`
// runs BaselineProfileGenerator on a connected API 33+ (or rooted) device and copies the result
// into gateway/todoList/src/release/generated/baselineProfiles/, which is committed and shipped.
android {
    namespace = "com.artemonre.onemoretodolist.baselineprofile"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        // Macrobenchmark's profile collection needs API 28+.
        minSdk = 28
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":gateway:todoList"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

baselineProfile {
    useConnectedDevices = true
}

// Hands the generator the tested app's real applicationId, so it never has to hardcode it.
androidComponents {
    onVariants { variant ->
        val artifactsLoader = variant.artifacts.getBuiltArtifactsLoader()
        variant.instrumentationRunnerArguments.put(
            "targetAppId",
            variant.testedApks.map { artifactsLoader.load(it)?.applicationId.orEmpty() }
        )
    }
}

dependencies {
    implementation(libs.androidx.testExt.junit)
    implementation(libs.androidx.benchmark.macro.junit4)
}
