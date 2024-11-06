package io.github.gmazzo.android.manifest.lock

import io.github.gmazzo.android.manifest.lock.Manifest.Entry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class ManifestReaderTest {

    private val source = File(javaClass.getResource("/AndroidManifest.xml")!!.file)


    @Test
    fun `parses a manifest correctly`() {
        val parsed = ManifestReader.parse(source)

        assertEquals("io.github.gmazzo.android.manifest.lock.test", parsed.namespace)
        assertEquals(24, parsed.minSDK)
        assertEquals(34, parsed.targetSDK)

        assertEquals(
            listOf(
                Entry(attributes = mapOf("reqNavigation" to setOf("trackball"))),
            ), parsed.configurations
        )

        assertEquals(
            listOf(
                Entry("android.permission.ACCESS_COARSE_LOCATION"),
                Entry("android.permission.ACCESS_FINE_LOCATION"),
                Entry("android.permission.ACCESS_NETWORK_STATE"),
                Entry("android.permission.ACCESS_WIFI_STATE"),
                Entry("android.permission.CALL_PHONE", mapOf("required" to setOf("false"))),
                Entry("android.permission.CAMERA", mapOf("required" to setOf("false"))),
                Entry("android.permission.DETECT_SCREEN_CAPTURE"),
                Entry("android.permission.FOREGROUND_SERVICE"),
                Entry("android.permission.FOREGROUND_SERVICE_CAMERA"),
                Entry("android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION"),
                Entry("android.permission.INTERNET"),
                Entry("android.permission.MODIFY_AUDIO_SETTINGS"),
                Entry("android.permission.POST_NOTIFICATIONS"),
                Entry("android.permission.READ_CONTACTS"),
                Entry("android.permission.READ_EXTERNAL_STORAGE", mapOf("maxSdkVersion" to setOf("32"))),
                Entry("android.permission.READ_MEDIA_IMAGES"),
                Entry("android.permission.READ_MEDIA_VIDEO"),
                Entry("android.permission.RECEIVE_BOOT_COMPLETED"),
                Entry("android.permission.RECORD_AUDIO"),
                Entry("android.permission.REORDER_TASKS"),
                Entry("android.permission.VIBRATE"),
                Entry("android.permission.WAKE_LOCK"),
                Entry("android.permission.WRITE_EXTERNAL_STORAGE", mapOf("maxSdkVersion" to setOf("28"))),
                Entry("com.google.android.c2dm.permission.RECEIVE"),
                Entry("com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE"),
                Entry("com.google.android.providers.gsf.permission.READ_GSERVICES")
            ), parsed.permissions
        )

        assertEquals(
            listOf(
                Entry(attributes = mapOf("glEsVersion" to setOf("0x00020000"), "required" to setOf("true"))),
                Entry("android.hardware.camera", mapOf("required" to setOf("false"))),
                Entry("android.hardware.camera.autofocus", mapOf("required" to setOf("false"))),
                Entry("android.hardware.telephony", mapOf("required" to setOf("false"))),
            ), parsed.features
        )

        assertEquals(
            listOf(
                Entry("android.ext.adservices", mapOf("required" to setOf("false"))),
                Entry("androidx.window.extensions", mapOf("required" to setOf("false"))),
                Entry("androidx.window.sidecar", mapOf("required" to setOf("false"))),
                Entry("org.apache.http.legacy", mapOf("required" to setOf("false"))),
            ), parsed.libraries
        )

        assertEquals(
            listOf(
                Entry("libnative-lib.so", mapOf("requiredBy" to setOf("manifest"))),
            ), parsed.nativeLibraries
        )

        assertEquals(
            mapOf(
                "activity" to setOf(
                    "com.testapp.onboarding.splash.SplashActivity",
                    "com.testapp.payments.core.processout.ProcessOutCallbackActivity"
                ),
                "service" to setOf("androidx.work.impl.background.systemjob.SystemJobService"),
                "receiver" to setOf("androidx.work.impl.diagnostics.DiagnosticsReceiver"),
            ), parsed.exports
        )
    }

    @Test
    fun `when disabled all features, parses a manifest correctly`() {
        val parsed = ManifestReader.parse(
            source,
            readSDKVersion = false,
            readConfigurations = false,
            readPermissions = false,
            readFeatures = false,
            readLibraries = false,
            readNativeLibraries = false,
            readExports = false
        )

        assertEquals(Manifest(namespace = "io.github.gmazzo.android.manifest.lock.test"), parsed)
    }

}
