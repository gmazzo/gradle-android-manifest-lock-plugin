package io.github.gmazzo.android.manifest.lock

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class AndroidManifestLockTask : DefaultTask() {

    @get:Internal
    abstract val variantManifests: MapProperty<String, RegularFile>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    @Suppress("LeakingThis", "unused")
    internal val variantManifestsFiles =
        variantManifests.map { it.values }

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
        val manifests = variantManifests.get()
            .mapValues { (_, manifest) ->
                ManifestReader.parse(
                    manifest = manifest.asFile,
                    readSDKVersion = contentSpec.sdkVersion.get(),
                    readPermissions = contentSpec.permissions.get(),
                    readFeatures = contentSpec.features.get(),
                    readLibraries = contentSpec.libraries.get(),
                    readExports = contentSpec.exports.get(),
                )
            }

        val lock = ManifestLockFactory.create(manifests)

        val file = lockFile.get().asFile
        val content = file.takeIf { it.exists() }?.readText()
        if (content != lock.content) {
            file.writeText(lock.content)

            val message = "${file.toRelativeString(rootDir)} has changed, please commit the updated lock file"
            if (failOnLockChange.get()) error(message) else logger.warn(message)
        }
    }

}
