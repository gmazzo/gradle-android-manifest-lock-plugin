package io.github.gmazzo.android.manifest.lock

import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class ManifestLockTest {

    private val main = Manifest(
        variantName = null,
        namespace = "org.test.app",
        minSDK = 14,
        targetSDK = 34,
        permissions = mapOf(
            "permission1" to emptyMap(),
            "permission2" to mapOf("required" to "true"),
            "permission2" to mapOf("required" to "true", "until" to "30"),
        ),
        features = mapOf(
            "feature1" to emptyMap(),
            "feature2" to mapOf("required" to "true"),
        ),
        libraries = mapOf(
            "lib1" to emptyMap(),
            "lib2" to mapOf("required" to "true"),
        ),
        exports = setOf("export1", "export2")
    )

    @Test
    fun `when all manifests are the same`() {
        val lock = ManifestLock(
            main.copy(variantName = "debug"),
            main.copy(variantName = "release"),
        )

        assertEquals(
            """
            NAMESPACE: org.test.app
            SDKS: 
              min=14
              target=34
            PERMISSIONS:
              permission1
              permission2: required=true, until=30
            FEATURES:
              feature1
              feature2: required=true
            LIBRARIES:
              lib1
              lib2: required=true
            EXPORTS:
              export1
              export2
            
            FINGERPRINT: 7c7c4f69739468b485fadb24a1f0564b
            
            """.trimIndent(),
            lock.content
        )
    }

    @Test
    fun `when one varies on package`() {
        val lock = ManifestLock(
            main.copy(variantName = "debug"),
            main.copy(variantName = "release", namespace = "org.test.app.release"),
        )

        assertEquals(
            """
            SDKS: 
              min=14
              target=34
            PERMISSIONS:
              permission1
              permission2: required=true, until=30
            FEATURES:
              feature1
              feature2: required=true
            LIBRARIES:
              lib1
              lib2: required=true
            EXPORTS:
              export1
              export2
            
            VARIANT: debug
              NAMESPACE: org.test.app
            
            VARIANT: release
              NAMESPACE: org.test.app.release
            
            FINGERPRINT: 98755187171e0f397fe74112f90214e2

            """.trimIndent(),
            lock.content
        )
    }

    @Test
    fun `when many has differences`() {
        val lock = ManifestLock(
            main.copy(
                variantName = "debug",
                minSDK = 10,
                features = main.features + ("feature2" to mapOf("required" to "false")) + ("debugFeature1" to emptyMap()),
                exports = main.exports + "debugExport1",
            ),
            main.copy(
                variantName = "release",
                namespace = "org.test.app.release",
                permissions = main.permissions + ("releasePermission1" to emptyMap()),
                features = main.features + ("releaseFeature1" to mapOf("required" to "true")),
                libraries = main.libraries + ("releaseLib1" to emptyMap()),
            ),
        )

        assertEquals(
            """
            SDKS: 
              target=34
            PERMISSIONS:
              permission1
              permission2: required=true, until=30
            FEATURES:
              feature1
            LIBRARIES:
              lib1
              lib2: required=true
            EXPORTS:
              export1
              export2
            
            VARIANT: debug
              NAMESPACE: org.test.app
              SDKS: 
                min=10
              FEATURES:
                feature2: required=false
                debugFeature1
              EXPORTS:
                debugExport1
            
            VARIANT: release
              NAMESPACE: org.test.app.release
              SDKS: 
                min=14
              PERMISSIONS:
                releasePermission1
              FEATURES:
                feature2: required=true
                releaseFeature1: required=true
              LIBRARIES:
                releaseLib1
            
            FINGERPRINT: a1ba4e5f61428b16e2470887281bf42c
            
            """.trimIndent(),
            lock.content
        )
    }

}