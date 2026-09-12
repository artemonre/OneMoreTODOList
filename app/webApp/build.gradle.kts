import java.util.Properties
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

// Same convention as gateway modules' keystore.properties/sentry.properties/posthog.properties:
// gitignored, per module, see posthog.properties.template. Missing file means a null API key,
// which Analytics.kt's initPostHog() treats as "disabled".
val posthogPropertiesFile = project.file("posthog.properties")
val posthogProperties = Properties().apply {
    if (posthogPropertiesFile.exists()) {
        posthogPropertiesFile.inputStream().use { load(it) }
    }
}
val posthogApiKey: String = posthogProperties.getProperty("apiKey", "")

val generatedPostHogConfigDir = layout.buildDirectory.dir("generated/posthog/kotlin")

// Registered via a local function (rather than referencing the script's top-level `val`s directly
// inside doLast) so the task action only captures plain function parameters - the config cache
// can't serialize a reference to the build script object itself.
fun registerGeneratePostHogConfig(apiKey: String, outputDir: Provider<Directory>) =
    tasks.register("generatePostHogConfig") {
        inputs.property("postHogApiKey", apiKey)
        outputs.dir(outputDir)
        doLast {
            val packageDir = outputDir.get().dir("com/artemonre/onemoretodolist/analytics").asFile
            packageDir.mkdirs()
            File(packageDir, "AnalyticsConfig.kt").writeText(
                """
                package com.artemonre.onemoretodolist.analytics

                internal val postHogApiKey: String? = ${if (apiKey.isBlank()) "null" else "\"$apiKey\""}

                """.trimIndent()
            )
        }
    }

val generatePostHogConfig = registerGeneratePostHogConfig(posthogApiKey, generatedPostHogConfigDir)

kotlin {
    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":app:shared"))

            implementation(libs.compose.ui)
        }
        commonMain {
            kotlin.srcDir(generatedPostHogConfigDir)
        }
    }
}

tasks.matching { it.name == "compileKotlinJs" || it.name == "compileKotlinWasmJs" }.configureEach {
    dependsOn(generatePostHogConfig)
}