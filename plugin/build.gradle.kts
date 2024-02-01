plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.samReceiver)
    alias(libs.plugins.gradle.pluginPublish)
    alias(libs.plugins.publicationsReport)
}

group = "io.github.gmazzo.android.manifest.lock"
description = "A gradle Gradle to control what Permissions, SDK-level, and other PlayStore listing sensitive settings is added into the Android Manifest"
version = providers
    .exec { commandLine("git", "describe", "--tags", "--always") }
    .standardOutput.asText.get().trim().removePrefix("v")

java.toolchain.languageVersion.set(JavaLanguageVersion.of(11))

gradlePlugin {
    website.set("https://github.com/gmazzo/gradle-android-manifest-lock-plugin")
    vcsUrl.set("https://github.com/gmazzo/gradle-android-manifest-lock-plugin")

    plugins.create("manifestLock") {
        id = "io.github.gmazzo.android.manifest.lock"
        displayName = name
        description = project.description
        implementationClass = "io.github.gmazzo.android.manifest.lock.GradleAndroidManifestLockPluginPlugin"
        tags.addAll("android", "agp", "manifest", "lock", "lockfile")
    }
}

samWithReceiver.annotation(HasImplicitReceiver::class.qualifiedName!!)

testing.suites.withType<JvmTestSuite> {
    useKotlinTest()
}

dependencies {
    fun DependencyHandler.plugin(dependency: Provider<PluginDependency>) =
        dependency.get().run { create("$pluginId:$pluginId.gradle.plugin:$version") }

    compileOnly(gradleKotlinDsl())
    compileOnly(plugin(libs.plugins.android))
}

tasks.publish {
    dependsOn(tasks.publishPlugins)
}
