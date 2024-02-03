package io.github.gmazzo.android.manifest.lock

import org.gradle.kotlin.dsl.apply
import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

class AndroidManifestLockPluginTest {

    @Test
    fun `plugin applies correctly on application`() =
        `plugin applies correctly`("com.android.application")

    @Test
    fun `plugin applies correctly on library`() =
        `plugin applies correctly`("com.android.library")

    private fun `plugin applies correctly`(androidPlugin: String) {
        val project = ProjectBuilder.builder().build()
        project.apply(plugin = androidPlugin)
        project.apply(plugin = "io.github.gmazzo.android.manifest.lock")

        assertNotNull(project.tasks.findByName("androidManifestLock"))
    }

}
