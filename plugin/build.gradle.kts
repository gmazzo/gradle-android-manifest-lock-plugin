@file:OptIn(ExperimentalAbiValidation::class)

import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.samReceiver)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.gitVersion)
    alias(libs.plugins.gradle.pluginPublish)
    alias(libs.plugins.publicationsReport)
    jacoco
}

group = "io.github.gmazzo.android.manifest.lock"
description =
    "A gradle Gradle to control what Permissions, SDK-level, and other PlayStore listing sensitive settings is added into the Android Manifest"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
samWithReceiver.annotation(HasImplicitReceiver::class.qualifiedName!!)

kotlin {
    abiValidation.enabled = true
    compilerOptions.jvmDefault = JvmDefaultMode.NO_COMPATIBILITY
    explicitApi()
}

val originUrl = providers
    .exec { commandLine("git", "remote", "get-url", "origin") }
    .standardOutput.asText.map { it.trim() }

gradlePlugin {
    website = originUrl
    vcsUrl = originUrl

    plugins.create("manifestLock") {
        id = "io.github.gmazzo.android.manifest.lock"
        displayName = name
        description = project.description
        implementationClass = "io.github.gmazzo.android.manifest.lock.AndroidManifestLockPlugin"
        tags.addAll("android", "agp", "manifest", "lock", "lockfile")
    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)

    pom {
        name = "${rootProject.name}-${project.name}"
        description = provider { project.description }
        url = originUrl

        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/license/mit/"
            }
        }

        developers {
            developer {
                id = "gmazzo"
                name = id
                email = "gmazzo65@gmail.com"
            }
        }

        scm {
            connection = originUrl
            developerConnection = originUrl
            url = originUrl
        }
    }
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

testing.suites.withType<JvmTestSuite> {
    useKotlinTest(libs.versions.kotlin)
}

tasks.test {
    environment("TEMP_DIR", temporaryDir)
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    reports.xml.required = true
}

afterEvaluate {
    tasks.named<Jar>("javadocJar") {
        from(tasks.dokkaGeneratePublicationJavadoc)
    }
}

tasks.withType<PublishToMavenRepository>().configureEach {
    mustRunAfter(tasks.publishPlugins)
}

tasks.validatePlugins {
    enableStricterValidation = true
}

tasks.check {
    dependsOn(tasks.checkLegacyAbi)
}

tasks.publishPlugins {
    enabled = "$version".matches("\\d+(\\.\\d+)+".toRegex())
}

tasks.publish {
    dependsOn(tasks.publishPlugins)
}
