package io.github.gmazzo.android.manifest.lock

import org.gradle.kotlin.dsl.apply
import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

class AndroidManifestLockPluginTest {

    @Test
    fun `plugin applies correctly`() {
        val project = ProjectBuilder.builder().build()
        project.apply(plugin = "io.github.gmazzo.android.manifest.lock")

        assertNotNull(project.tasks.findByName("greeting"))
    }

}
