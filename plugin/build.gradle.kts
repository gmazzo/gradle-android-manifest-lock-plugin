plugins {
    `java-gradle-plugin`
    id("org.jetbrains.kotlin.jvm") version "1.8.20"
}

gradlePlugin.plugins.create("manifestLock") {
    id = "io.github.gmazzo.android.manifest.lock.greeting"
    implementationClass = "io.github.gmazzo.android.manifest.lock.GradleAndroidManifestLockPluginPlugin"
}

testing.suites.withType<JvmTestSuite> {
    useKotlinTest()
}
