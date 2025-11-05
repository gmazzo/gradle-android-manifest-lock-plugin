package io.github.gmazzo.android.manifest.lock

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.dsl.AndroidSourceSet
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.internal.publishing.AndroidArtifacts.ArtifactType
import java.io.File
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.type.ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE
import org.gradle.api.file.RegularFile
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registerTransform
import org.gradle.kotlin.dsl.withGroovyBuilder

class AndroidManifestLockPlugin : Plugin<Project> {

    override fun apply(project: Project) = project.plugins.withId("com.android.base") {
        val android: ExtensionAware by project.extensions
        val androidComponents: AndroidComponentsExtension<*, *, *> by project.extensions

        val defaultLockFile = android.mainManifest.map { file ->
            val lock = file.resolveSibling("${file.nameWithoutExtension}.lock.yaml")
            project.layout.projectDirectory.file(lock.path)
        }
        val extension = android.createExtension(defaultLockFile)
        val manifests = project.objects.mapProperty<String, RegularFile>()
        val runtimeDependencies = project.objects.mapProperty<String, ArtifactCollection>()

        project.dependencies.registerTransform(NativeLibrariesReportTransform::class) {
            from.attribute(ARTIFACT_TYPE_ATTRIBUTE, ArtifactType.AAR.type)
            to.attribute(ARTIFACT_TYPE_ATTRIBUTE, JNI_REPORT_ARTIFACT_TYPE)
        }

        androidComponents.onVariants(androidComponents.selector().all()) {
            manifests.put(
                it.name,
                it.artifacts.get(SingleArtifact.MERGED_MANIFEST),
            )

            runtimeDependencies.put(
                it.name,
                extension.content.nativeLibraries.map { enabled ->
                    it.runtimeConfiguration.incoming
                        .artifactView {
                            attributes.attribute(
                                ARTIFACT_TYPE_ATTRIBUTE,
                                JNI_REPORT_ARTIFACT_TYPE
                            )
                        }
                        .artifacts
                }
            )
        }

        val lockTask = project.tasks.register<AndroidManifestLockTask>("androidManifestLock") {
            variantManifests.set(manifests)
            variantRuntimeClasspath.set(
                extension.content.nativeLibraries
                    .flatMap { if (it) runtimeDependencies else null })
            manifestContent.set(extension.content)
            lockFile.set(extension.lockFile)
            failOnLockChange.set(extension.failOnLockChange)
        }

        project.tasks.named("check") {
            dependsOn(lockTask)
        }
    }

    private fun ExtensionAware.createExtension(defaultLockFile: Provider<RegularFile>) =
        extensions
            .create<AndroidManifestLockExtension>("manifestLock")
            .apply {

                content {

                    sdkVersion
                        .convention(true)
                        .finalizeValueOnRead()

                    configurations
                        .convention(true)
                        .finalizeValueOnRead()

                    permissions
                        .convention(true)
                        .finalizeValueOnRead()

                    features
                        .convention(true)
                        .finalizeValueOnRead()

                    libraries
                        .convention(true)
                        .finalizeValueOnRead()

                    nativeLibraries
                        .convention(true)
                        .finalizeValueOnRead()

                    exports
                        .convention(true)
                        .finalizeValueOnRead()

                }

                lockFile
                    .convention(defaultLockFile)
                    .finalizeValueOnRead()

                failOnLockChange
                    .convention(false)
                    .finalizeValueOnRead()

            }

    @Suppress("UNCHECKED_CAST")
    private val ExtensionAware.mainManifest: Provider<File>
        get() = withGroovyBuilder { getProperty("sourceSets") as NamedDomainObjectContainer<AndroidSourceSet> }
            .named("main")
            .map { it.manifest.withGroovyBuilder { getProperty("srcFile") as File } }

    companion object {
        const val JNI_REPORT_ARTIFACT_TYPE = "jniReport"
    }

}
