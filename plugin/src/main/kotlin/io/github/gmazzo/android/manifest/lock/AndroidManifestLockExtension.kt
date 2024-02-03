package io.github.gmazzo.android.manifest.lock

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

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

}
