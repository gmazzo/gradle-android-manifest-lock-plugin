package io.github.gmazzo.android.manifest.lock

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

fun main() = ManifestReaderTest().`parses a manifest correctly`()

class ManifestReaderTest {

    @Test
    fun `parses a manifest correctly`() {
        val source = File(javaClass.getResource("/AndroidManifest.xml")!!.file)
        val parsed = ManifestReader.parse(source)

        assertEquals("io.github.gmazzo.android.manifest.lock.test", parsed.namespace)
        assertEquals(24, parsed.minSDK)
        assertEquals(34, parsed.targetSDK)

        assertEquals(
            mapOf(
                "android.permission.ACCESS_COARSE_LOCATION" to emptyMap(),
                "android.permission.ACCESS_FINE_LOCATION" to emptyMap(),
                "android.permission.ACCESS_NETWORK_STATE" to emptyMap(),
                "android.permission.ACCESS_WIFI_STATE" to emptyMap(),
                "android.permission.CALL_PHONE" to mapOf("required" to "false"),
                "android.permission.CAMERA" to mapOf("required" to "false"),
                "android.permission.DETECT_SCREEN_CAPTURE" to emptyMap(),
                "android.permission.FOREGROUND_SERVICE" to emptyMap(),
                "android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION" to emptyMap(),
                "android.permission.INTERNET" to emptyMap(),
                "android.permission.MODIFY_AUDIO_SETTINGS" to emptyMap(),
                "android.permission.POST_NOTIFICATIONS" to emptyMap(),
                "android.permission.READ_CONTACTS" to emptyMap(),
                "android.permission.READ_EXTERNAL_STORAGE" to mapOf("maxSdkVersion" to "32"),
                "android.permission.READ_MEDIA_IMAGES" to emptyMap(),
                "android.permission.READ_MEDIA_VIDEO" to emptyMap(),
                "android.permission.RECEIVE_BOOT_COMPLETED" to emptyMap(),
                "android.permission.RECORD_AUDIO" to emptyMap(),
                "android.permission.REORDER_TASKS" to emptyMap(),
                "android.permission.VIBRATE" to emptyMap(),
                "android.permission.WAKE_LOCK" to emptyMap(),
                "android.permission.WRITE_EXTERNAL_STORAGE" to mapOf("maxSdkVersion" to "28"),
                "com.google.android.c2dm.permission.RECEIVE" to emptyMap(),
                "com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE" to emptyMap(),
                "com.google.android.providers.gsf.permission.READ_GSERVICES" to emptyMap()
            ), parsed.permissions
        )

        assertEquals(
            mapOf(
                "" to mapOf("glEsVersion" to "0x00020000", "required" to "true"),
                "android.hardware.camera" to mapOf("required" to "false"),
                "android.hardware.camera.autofocus" to mapOf("required" to "false"),
                "android.hardware.telephony" to mapOf("required" to "false"),
            ), parsed.features
        )

        assertEquals(
            mapOf(
                "android.ext.adservices" to mapOf("required" to "false"),
                "androidx.window.extensions" to mapOf("required" to "false"),
                "androidx.window.sidecar" to mapOf("required" to "false"),
                "org.apache.http.legacy" to mapOf("required" to "false"),
            ), parsed.libraries
        )

        assertEquals(
            setOf(
                "activity#com.testapp.onboarding.splash.SplashActivity",
                "activity#com.testapp.payments.core.processout.ProcessOutCallbackActivity",
                "receiver#androidx.work.impl.diagnostics.DiagnosticsReceiver",
                "service#androidx.work.impl.background.systemjob.SystemJobService",
            ), parsed.exports
        )
    }

}
