package io.github.gmazzo.android.manifest.lock

import org.gradle.api.Action
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested

@JvmDefaultWithoutCompatibility
interface AndroidManifestLockExtension {

    /**
     * The location of the generated lock file.
     *
     * Defaults to `src/main/AndroidManifest.lock`
     */
    val lockFile: RegularFileProperty

    /**
     * The build will fail if the [lockFile] has changed based on the current manifest inputs.
     *
     * If the file does not exist already, it will be created, sill failing.
     *
     * Defaults to `false`
     */
    val failOnLockChange: Property<Boolean>

    @get:Nested
    val content: Content

    fun content(action: Action<Content>) =
        apply { action.execute(content) }

    interface Content {

        @get:Input
        val sdkVersion: Property<Boolean>

        @get:Input
        val permissions: Property<Boolean>

        @get:Input
        val features: Property<Boolean>

        @get:Input
        val libraries: Property<Boolean>

        @get:Input
        val exports: Property<Boolean>

    }

}
