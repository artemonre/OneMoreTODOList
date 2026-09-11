import java.util.Properties
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
}

group = "com.artemonre.onemoretodolist"
version = "1.0.0"
application {
    mainClass = "com.artemonre.onemoretodolist.ApplicationKt"
}

dependencies {
    api(project(":core"))
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverStatusPages)
    implementation(libs.sentry.java)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}

// Same convention as gateway modules' keystore.properties/sentry.properties: gitignored, per
// module, see sentry.properties.template. Missing file (a fresh checkout, CI without secrets)
// generates a null DSN, which Application.kt treats as "disabled".
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