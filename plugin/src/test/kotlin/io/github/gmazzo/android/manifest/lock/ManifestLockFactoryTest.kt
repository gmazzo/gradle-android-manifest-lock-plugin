package io.github.gmazzo.android.manifest.lock

import io.github.gmazzo.android.manifest.lock.Manifest.Entry
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class ManifestLockFactoryTest {

    private val main = Manifest(
        namespace = "org.test.app",
        minSDK = 14,
        targetSDK = 34,
        permissions = listOf(
            Entry("permission1"),
            Entry("permission2", mapOf("required" to "true")),
            Entry("permission2", mapOf("required" to "true", "until" to "30")),
        ),
        features = listOf(
            Entry("feature1"),
            Entry("feature2", mapOf("required" to "true")),
            Entry(attributes = mapOf("custom" to "1")),
        ),
        libraries = listOf(
            Entry("lib1"),
            Entry("lib2", mapOf("required" to "true")),
        ),
        exports = listOf(
            Entry("export1", mapOf("type" to "activity")),
            Entry("export2", mapOf("type" to "service")),
        )
    )

    @Test
    fun `when all manifests are the same`() {
        val lock = ManifestLockFactory.create(
            mapOf(
                "debug" to main,
                "release" to main,
            )
        )

        assertEquals(
            """
            main:
              namespace: org.test.app
              minSDK: 14
              targetSDK: 34
              permissions:
              - permission1
              - permission2:
                  required: true
              - permission2:
                  required: true
                  until: 30
              features:
              - feature1
              - feature2:
                  required: true
              - custom: 1
              libraries:
              - lib1
              - lib2:
                  required: true
              exports:
              - export1:
                  type: activity
              - export2:
                  type: service
            fingerprint: d69a81c5982e1cb8864d6fa9f8a21589
            
            """.trimIndent(),
            lock.content
        )
    }

    @Test
    fun `when one varies on package`() {
        val lock = ManifestLockFactory.create(
            mapOf(
                "debug" to main,
                "release" to main.copy(namespace = "org.test.app.release"),
            )
        )

        assertEquals(
            """
            main:
              minSDK: 14
              targetSDK: 34
              permissions:
              - permission1
              - permission2:
                  required: true
              - permission2:
                  required: true
                  until: 30
              features:
              - feature1
              - feature2:
                  required: true
              - custom: 1
              libraries:
              - lib1
              - lib2:
                  required: true
              exports:
              - export1:
                  type: activity
              - export2:
                  type: service
            variants:
              debug:
                namespace: org.test.app
              release:
                namespace: org.test.app.release
            fingerprint: 5e78b11162c281bd4743c57167e6cc43
            
            """.trimIndent(),
            lock.content
        )
    }

    @Test
    fun `when many has differences`() {
        val lock = ManifestLockFactory.create(
            mapOf(
                "debug" to main.copy(
                    minSDK = 10,
                    features = main.features.orEmpty() + Entry("feature2", mapOf("required" to "false")) + Entry("debugFeature1"),
                    exports = main.exports.orEmpty() + Entry("debugExport1", mapOf("type" to "activity")),
                ),
                "release" to main.copy(
                    namespace = "org.test.app.release",
                    permissions = main.permissions.orEmpty() + Entry("releasePermission1"),
                    features = main.features.orEmpty() + Entry("releaseFeature1" , mapOf("required" to "true")),
                    libraries = main.libraries.orEmpty() + Entry("releaseLib1"),
                ),
            )
        )

        assertEquals(
            """
            main:
              targetSDK: 34
              permissions:
              - permission1
              - permission2:
                  required: true
              - permission2:
                  required: true
                  until: 30
              features:
              - feature1
              - feature2:
                  required: true
              - custom: 1
              libraries:
              - lib1
              - lib2:
                  required: true
              exports:
              - export1:
                  type: activity
              - export2:
                  type: service
            variants:
              debug:
                namespace: org.test.app
                minSDK: 10
                features:
                - feature2:
                    required: false
                - debugFeature1
                exports:
                - debugExport1:
                    type: activity
              release:
                namespace: org.test.app.release
                minSDK: 14
                permissions:
                - releasePermission1
                features:
                - releaseFeature1:
                    required: true
                libraries:
                - releaseLib1
            fingerprint: 85d891ef3bc41dfba1d47e1953ec4ede
            
            """.trimIndent(),
            lock.content
        )
    }

}