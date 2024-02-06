package io.github.gmazzo.android.manifest.lock

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.register
import java.io.File

class AndroidManifestLockPlugin : Plugin<Project> {

    override fun apply(project: Project) = project.plugins.withId("com.android.base") {
        val android: BaseExtension by project.extensions
        val androidComponents: AndroidComponentsExtension<*, *, *> by project.extensions

        val manifest = android.sourceSets.named("main").map { it.manifest.srcFile }
        val manifests = project.objects.mapProperty<String, RegularFile>()

        androidComponents.onVariants(androidComponents.selector().all()) {
            manifests.put(
                it.name,
                it.artifacts.get(SingleArtifact.MERGED_MANIFEST),
            )
        }

        val extension = project.createExtension(android, manifest)
        val lockTask = project.tasks.register<AndroidManifestLockTask>("androidManifestLock") {
            variantManifests.set(manifests)
            lockFile.set(extension.lockFile)
            failOnLockChange.set(extension.failOnLockChange)
        }

        project.tasks.named("check") {
            dependsOn(lockTask)
        }
    }

    private fun Project.createExtension(android: BaseExtension, manifest: Provider<File>) = (android as ExtensionAware)
        .extensions
        .create<AndroidManifestLockExtension>("manifestLock")
        .apply {

            lockFile
                .convention(manifest.map { file ->
                    val lock = file.resolveSibling("${file.nameWithoutExtension}.lock.yaml")
                    layout.projectDirectory.file(lock.path)
                })
                .finalizeValueOnRead()

            failOnLockChange
                .convention(false)
                .finalizeValueOnRead()

        }

}
