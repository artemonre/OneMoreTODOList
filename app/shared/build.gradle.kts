import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidxRoom3)
}

kotlin {
    applyDefaultHierarchyTemplate()

    // Room's @ConstructedBy pattern relies on an expect object with a KSP-generated
    // actual per target - still Beta, silence the warning it triggers project-wide.
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    jvm()
    
    js {
        browser()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    
    android {
       namespace = "com.artemonre.onemoretodolist.app.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }
    
    sourceSets {
        val commonMain by getting
        val androidMain by getting
        val iosMain by getting
        val jvmMain by getting
        val jsMain by getting

        // Room is only wired up for targets with a Room-supported SQLite driver
        // (Android/iOS/JVM). JS/WasmJs get a plain in-memory data source via webMain
        // instead of the WebWorker/OPFS driver setup Room's web support requires.
        val roomMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.androidx.room3.runtime)
                implementation(libs.androidx.sqlite.bundled)
                // Checklist items and tags are stored as JSON text columns - see TodoMappers.
                implementation(libs.kotlinx.serialization.json)
            }
        }
        androidMain.dependsOn(roomMain)
        iosMain.dependsOn(roomMain)
        jvmMain.dependsOn(roomMain)

        // The default hierarchy template already provides a webMain source set
        // grouping js + wasmJs.
        val webMain by getting

        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.work.runtime.ktx)
            implementation(libs.play.appUpdate.ktx)
            // Photo -> todos: CameraX viewfinder/capture plus the bundled (offline, no Play
            // Services download) ML Kit Latin text recognition model.
            implementation(libs.androidx.camera.core)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.compose)
            implementation(libs.mlkit.textRecognition)
            // ML Kit has no Cyrillic model on Android - Tesseract covers Russian/Serbian Cyrillic,
            // with its models bundled under androidMain/assets/tessdata.
            implementation(libs.tesseract4android)
        }
        commonMain.dependencies {
            api(project(":core"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.serialization.core)
            // api: TodoFormBody's onConfirm param exposes LocalDate/LocalTime to downstream
            // consumers (gateway/*'s QuickAddTodoActivity) that implement it.
            api(libs.kotlinx.datetime)
            implementation(libs.navigation3.ui)
            // api: App()'s platformModules param exposes Koin's Module type to downstream
            // consumers (gateway/*, desktopApp, webApp, iosApp) that construct it.
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.compose.materialIconsCore)
            implementation(libs.compose.materialIconsExtended)
            implementation(libs.reorderable)
            implementation(libs.multiplatformSettings.noArg)
            implementation(libs.multiplatformSettings.coroutines)
            implementation(libs.multiplatformSettings.makeObservable)
            implementation(libs.sentry.kmp)
            // api: initPostHog()'s context param exposes PostHogContext to downstream consumers
            // (gateway/*, desktopApp, webApp, iosApp) that construct it - same reasoning as koin-core
            // above.
            api(libs.posthog.kmp)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        jvmTest.dependencies {
            implementation(libs.androidx.room3.testing)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
        }
    }
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
    add("kspAndroid", libs.androidx.room3.compiler)
    add("kspIosArm64", libs.androidx.room3.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room3.compiler)
    add("kspJvm", libs.androidx.room3.compiler)
}