plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("io.github.gmazzo.android.manifest.lock")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

android {
    namespace = "io.github.gmazzo.android.manifest.lock.demo"

    defaultConfig {
        compileSdk = 34
        minSdk = 24
        targetSdk = compileSdk
    }
}

dependencies {
    implementation(libs.androidx.workManager)
    debugImplementation(libs.leakCanary)
    releaseImplementation(libs.google.services.maps)
}
