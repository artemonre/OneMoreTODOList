import java.util.Properties
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":app:shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

// Same convention as gateway modules' keystore.properties/sentry.properties: gitignored, per
// module, see sentry.properties.template. Missing file (a fresh checkout, CI without secrets)
// generates a null DSN, which Observability.kt's initSentry() treats as "disabled". Plain JVM apps
// don't have Android's BuildConfig mechanism, so the value is injected via a generated Kotlin file
// instead.
val sentryPropertiesFile = project.file("sentry.properties")
val sentryProperties = Properties().apply {
    if (sentryPropertiesFile.exists()) {
        sentryPropertiesFile.inputStream().use { load(it) }
    }
}
val sentryDsn: String = sentryProperties.getProperty("dsn", "")

val generatedSentryConfigDir = layout.buildDirectory.dir("generated/sentry/kotlin")

// Registered via a local function (rather than referencing the script's top-level `val`s directly
// inside doLast) so the task action only captures plain function parameters - the config cache
// can't serialize a reference to the build script object itself.
fun registerGenerateSentryConfig(dsn: String, outputDir: Provider<Directory>) =
    tasks.register("generateSentryConfig") {
        outputs.dir(outputDir)
        doLast {
            val packageDir = outputDir.get().dir("com/artemonre/onemoretodolist/observability").asFile
            packageDir.mkdirs()
            File(packageDir, "SentryConfig.kt").writeText(
                """
                package com.artemonre.onemoretodolist.observability

                internal val sentryDsn: String? = ${if (dsn.isBlank()) "null" else "\"$dsn\""}

                """.trimIndent()
            )
        }
    }

val generateSentryConfig = registerGenerateSentryConfig(sentryDsn, generatedSentryConfigDir)

kotlin {
    sourceSets.main {
        kotlin.srcDir(generatedSentryConfigDir)
    }
}

tasks.named("compileKotlin") {
    dependsOn(generateSentryConfig)
}

compose.desktop {
    application {
        mainClass = "com.artemonre.onemoretodolist.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.artemonre.onemoretodolist"
            packageVersion = "1.0.0"
        }
    }
}