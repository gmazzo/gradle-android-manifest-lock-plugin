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
        exports = mapOf(
            "activity" to setOf("export1"),
            "service" to setOf("export2"),
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
                activity:
                - export1
                service:
                - export2
            fingerprint: 119f92fb3493aeaaadffb717199050e7

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
                activity:
                - export1
                service:
                - export2
            variants:
              debug:
                namespace: org.test.app
              release:
                namespace: org.test.app.release
            fingerprint: 8b4b97542b3e84216505ddb6161610d4

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
                    features = main.features!! + Entry("feature2", mapOf("required" to "false")) + Entry("debugFeature1"),
                    exports = mapOf("activity" to main.exports!!["activity"]!! + "debugExport1") + main.exports.filterKeys { it != "activity" },
                ),
                "release" to main.copy(
                    namespace = "org.test.app.release",
                    permissions = main.permissions!! + Entry("releasePermission1"),
                    features = main.features + Entry("releaseFeature1" , mapOf("required" to "true")),
                    libraries = main.libraries!! + Entry("releaseLib1"),
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
                activity:
                - export1
                service:
                - export2
            variants:
              debug:
                namespace: org.test.app
                minSDK: 10
                features:
                - feature2:
                    required: false
                - debugFeature1
                exports:
                  activity:
                  - debugExport1
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
            fingerprint: 575faf711192e7fe52f7e0577e81e574

            """.trimIndent(),
            lock.content
        )
    }

}