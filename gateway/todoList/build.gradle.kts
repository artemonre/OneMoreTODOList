plugins {
    id("gateway.application")
}

android {
    namespace = "com.artemonre.onemoretodolist"
    defaultConfig {
        applicationId = "com.artemonre.onemoretodolist"
    }
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
