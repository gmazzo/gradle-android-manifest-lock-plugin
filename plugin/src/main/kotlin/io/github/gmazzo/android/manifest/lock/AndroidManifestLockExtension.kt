package io.github.gmazzo.android.manifest.lock

import org.gradle.api.Action
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested

public interface AndroidManifestLockExtension {

    /**
     * The location of the generated lock file.
     *
     * Defaults to `src/main/AndroidManifest.lock`
     */
    public val lockFile: RegularFileProperty

    /**
     * The build will fail if the [lockFile] has changed based on the current manifest inputs.
     *
     * If the file does not exist already, it will be created, sill failing.
     *
     * Defaults to `false`
     */
    public val failOnLockChange: Property<Boolean>

    @get:Nested
    public val content: Content

    public fun content(action: Action<Content>): AndroidManifestLockExtension =
        apply { action.execute(content) }

    public interface Content {

        @get:Input
        public val sdkVersion: Property<Boolean>

        @get:Input
        public val configurations: Property<Boolean>

        @get:Input
        public val permissions: Property<Boolean>

        @get:Input
        public val features: Property<Boolean>

        @get:Input
        public val libraries: Property<Boolean>

        @get:Input
        public val nativeLibraries: Property<Boolean>

        @get:Input
        public val exports: Property<Boolean>

    }

}
