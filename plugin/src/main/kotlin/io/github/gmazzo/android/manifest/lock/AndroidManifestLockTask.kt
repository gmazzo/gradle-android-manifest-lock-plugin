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

    @get:OutputFile
    abstract val lockFile: RegularFileProperty

    @get:Input
    abstract val failOnLockChange: Property<Boolean>

    private val rootDir = project.rootDir

    @TaskAction
    fun generateLock() {
        val manifests = variantManifests.get()
            .entries
            .map { (variantName, manifest) -> ManifestReader.parse(manifest.asFile, variantName) }

        val lock = ManifestLock(manifests)

        val file = lockFile.get().asFile
        val content = file.takeIf { it.exists() }?.readText()
        if (content != lock.content) {
            file.writeText(lock.content)

            val message = "${file.toRelativeString(rootDir)} has changed, please commit the updated lock file"
            if (failOnLockChange.get()) error(message) else logger.warn(message)
        }
    }

}
