package io.github.gmazzo.android.manifest.lock

import com.charleskorn.kaml.decodeFromStream
import io.github.gmazzo.android.manifest.lock.ManifestLockFactory.yaml
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class AndroidManifestLockTask : DefaultTask() {

    @get:Internal
    abstract val variantManifests: MapProperty<String, RegularFile>

    @get:Input
    internal val variantManifestsFileNames =
        variantManifests.map { it.mapValues { (_, file) -> file.asFile.toRelativeString(rootDir) } }

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    internal val variantManifestsFileContents =
        variantManifests.map { it.values }

    @get:Internal
    abstract val variantRuntimeClasspath: MapProperty<String, ArtifactCollection>

    @get:Input
    internal val variantRuntimeClasspathFileNames =
        variantRuntimeClasspath.map {
            it.mapValues { (_, files) -> files.artifactFiles.files.map { file -> file.name } }
        }

    @get:Classpath
    @get:Optional
    internal val variantRuntimeClasspathFileContents =
        variantRuntimeClasspath.map { it.values.flatMap { it.artifactFiles } }

    @get:Nested
    abstract val manifestContent: Property<AndroidManifestLockExtension.Content>

    @get:OutputFile
    abstract val lockFile: RegularFileProperty

    @get:Input
    abstract val failOnLockChange: Property<Boolean>

    private val rootDir = project.rootDir

    @TaskAction
    fun generateLock() {
        val contentSpec = manifestContent.get()
        val nativeLibraries by lazy { computeNativeLibraries() }
        val manifests = variantManifests.get()
            .mapValues { (variant, manifest) ->
                ManifestReader.parse(
                    manifest = manifest.asFile,
                    readSDKVersion = contentSpec.sdkVersion.get(),
                    readPermissions = contentSpec.permissions.get(),
                    readFeatures = contentSpec.features.get(),
                    readLibraries = contentSpec.libraries.get(),
                    readExports = contentSpec.exports.get(),
                ).copy(
                    nativeLibraries =
                    if (contentSpec.nativeLibraries.get()) nativeLibraries[variant]
                    else null,
                )
            }

        val lock = ManifestLockFactory.create(manifests)

        val file = lockFile.get().asFile
        val content = file.takeIf { it.exists() }?.readText()
        if (content != lock.content) {
            file.writeText(lock.content)

            val message =
                "${file.toRelativeString(rootDir)} has changed, please commit the updated lock file"
            if (failOnLockChange.get()) error(message) else logger.warn(message)
        }
    }

    private fun computeNativeLibraries(): Map<String, List<Map<String, NativeLibrary>>?> =
        variantRuntimeClasspath.get().mapValues { (_, artifacts) ->
            artifacts.artifacts.mapNotNull {
                val libraries: NativeLibrary = it.file.inputStream().use(yaml::decodeFromStream)

                if (libraries.isNotEmpty())
                    mapOf(it.id.dependencyName to libraries)
                else null
            }.takeIf { it.isNotEmpty() }
        }

    private val ComponentArtifactIdentifier.dependencyName get() = when(val id = componentIdentifier) {
        is ModuleComponentIdentifier -> "${id.group}:${id.module}"
        is ProjectComponentIdentifier -> id.projectPath
        else -> id.displayName
    }

}
