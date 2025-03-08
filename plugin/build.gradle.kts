plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.samReceiver)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.gradle.pluginPublish)
    alias(libs.plugins.publicationsReport)
    signing
    jacoco
}

group = "io.github.gmazzo.android.manifest.lock"
description = "A gradle Gradle to control what Permissions, SDK-level, and other PlayStore listing sensitive settings is added into the Android Manifest"
version = providers
    .exec { commandLine("git", "describe", "--tags", "--always") }
    .standardOutput.asText.get().trim().removePrefix("v")

java.toolchain.languageVersion.set(JavaLanguageVersion.of(11))
kotlin.compilerOptions.freeCompilerArgs.add("-Xjvm-default=all")
samWithReceiver.annotation(HasImplicitReceiver::class.qualifiedName!!)

gradlePlugin {
    website.set("https://github.com/gmazzo/gradle-android-manifest-lock-plugin")
    vcsUrl.set("https://github.com/gmazzo/gradle-android-manifest-lock-plugin")

    plugins.create("manifestLock") {
        id = "io.github.gmazzo.android.manifest.lock"
        displayName = name
        description = project.description
        implementationClass = "io.github.gmazzo.android.manifest.lock.AndroidManifestLockPlugin"
        tags.addAll("android", "agp", "manifest", "lock", "lockfile")
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project

    useInMemoryPgpKeys(signingKey, signingPassword)
    publishing.publications.configureEach(::sign)
    tasks.withType<Sign>().configureEach { enabled = signingKey != null }
}

testing.suites.withType<JvmTestSuite> {
    useKotlinTest(libs.versions.kotlin)
}

dependencies {
    fun DependencyHandler.plugin(dependency: Provider<PluginDependency>) =
        dependency.get().run { create("$pluginId:$pluginId.gradle.plugin:$version") }

    compileOnly(gradleKotlinDsl())
    compileOnly(plugin(libs.plugins.android.library))

    implementation(libs.diffUtils)
    implementation(libs.kotlin.serialization.yaml)

    testImplementation(gradleKotlinDsl())
    testImplementation(plugin(libs.plugins.android.library))
}

tasks.test {
    environment("TEMP_DIR", temporaryDir)
    javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(17) }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    reports.xml.required = true
}

tasks.publish {
    dependsOn(tasks.publishPlugins)
}
