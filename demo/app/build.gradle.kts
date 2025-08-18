plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("io.github.gmazzo.android.manifest.lock")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))

android {
    namespace = "io.github.gmazzo.android.manifest.lock.demo"

    manifestLock {

        failOnLockChange = providers
            .environmentVariable("CI")
            .map(String::toBoolean)
            .orElse(false)

    }

    defaultConfig {
        compileSdk = 35
        minSdk = 24
        targetSdk = compileSdk
    }
}

dependencies {
    implementation(libs.androidx.camera)
    implementation(libs.androidx.workManager)
    debugImplementation(libs.leakCanary)
    releaseImplementation(libs.google.services.maps)
    releaseImplementation(libs.firebase.crashlytics.ndk)
}
