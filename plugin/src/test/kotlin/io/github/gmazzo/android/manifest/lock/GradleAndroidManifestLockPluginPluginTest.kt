package io.github.gmazzo.android.manifest.lock

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

class GradleAndroidManifestLockPluginPluginTest {

    @Test
    fun `plugin registers task`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("io.github.gmazzo.android.manifest.lock.greeting")

        // Verify the result
        assertNotNull(project.tasks.findByName("greeting"))
    }

}
