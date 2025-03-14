package io.github.gmazzo.android.manifest.lock

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

class AndroidManifestLockTaskTest {

    private val tempDir = File(System.getenv("TEMP_DIR")!!, "taskTest").apply {
        deleteRecursively()
        mkdirs()
    }

    private val manifestFile = tempDir.resolve("AndroidManifest.xml").also {
        it.outputStream().use(javaClass.getResource("/AndroidManifest.xml")!!.openStream()::copyTo)
    }

    private val manifestDependencyFile =
        tempDir.resolve("AndroidManifest.dependecy.jni.yaml").also {
            it.outputStream().use(
                javaClass.getResource("/AndroidManifest.dependecy.jni.yaml")!!.openStream()::copyTo
            )
        }

    private val manifestLockFile = tempDir.resolve("AndroidManifest.lock.yaml")

    private val task = with(
        ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .withGradleUserHomeDir(tempDir.resolve(".home"))
            .build()
    ) {

        val debugDependencies by configurations.creating

        dependencies {
            debugDependencies(files(manifestDependencyFile))
        }

        tasks.register<AndroidManifestLockTask>("testTask") {
            variantManifests.put("debug", layout.file(provider { manifestFile }))
            variantManifests.put("release", layout.file(provider { manifestFile }))

            variantRuntimeClasspath.put("debug", debugDependencies.incoming.artifacts)

            manifestContent.set(objects.newInstance<AndroidManifestLockExtension.Content>().apply {
                sdkVersion.set(true)
                configurations.set(true)
                permissions.set(true)
                features.set(true)
                libraries.set(true)
                nativeLibraries.set(true)
                exports.set(true)
            })

            lockFile.set(manifestLockFile)
            failOnLockChange.set(false)
        }
    }

    @Test
    fun `lock tasks generates a lock file`() {
        task.get().generateLock()

        assertEquals(
            javaClass.getResource("/AndroidManifest.lock.yaml")!!.readText(),
            manifestLockFile.readText(),
        )
    }

}
