package io.github.gmazzo.android.manifest.lock

import io.github.gmazzo.android.manifest.lock.Manifest.Entry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class ManifestReaderTest {

    @Test
    fun `parses a manifest correctly`() {
        val source = File(javaClass.getResource("/AndroidManifest.xml")!!.file)
        val parsed = ManifestReader.parse(source)

        assertEquals("io.github.gmazzo.android.manifest.lock.test", parsed.namespace)
        assertEquals(24, parsed.minSDK)
        assertEquals(34, parsed.targetSDK)

        assertEquals(
            listOf(
                Entry("android.permission.ACCESS_COARSE_LOCATION"),
                Entry("android.permission.ACCESS_FINE_LOCATION"),
                Entry("android.permission.ACCESS_NETWORK_STATE"),
                Entry("android.permission.ACCESS_WIFI_STATE"),
                Entry("android.permission.CALL_PHONE", mapOf("required" to "false")),
                Entry("android.permission.CAMERA", mapOf("required" to "false")),
                Entry("android.permission.DETECT_SCREEN_CAPTURE"),
                Entry("android.permission.FOREGROUND_SERVICE"),
                Entry("android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION"),
                Entry("android.permission.INTERNET"),
                Entry("android.permission.MODIFY_AUDIO_SETTINGS"),
                Entry("android.permission.POST_NOTIFICATIONS"),
                Entry("android.permission.READ_CONTACTS"),
                Entry("android.permission.READ_EXTERNAL_STORAGE", mapOf("maxSdkVersion" to "32")),
                Entry("android.permission.READ_MEDIA_IMAGES"),
                Entry("android.permission.READ_MEDIA_VIDEO"),
                Entry("android.permission.RECEIVE_BOOT_COMPLETED"),
                Entry("android.permission.RECORD_AUDIO"),
                Entry("android.permission.REORDER_TASKS"),
                Entry("android.permission.VIBRATE"),
                Entry("android.permission.WAKE_LOCK"),
                Entry("android.permission.WRITE_EXTERNAL_STORAGE", mapOf("maxSdkVersion" to "28")),
                Entry("com.google.android.c2dm.permission.RECEIVE"),
                Entry("com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE"),
                Entry("com.google.android.providers.gsf.permission.READ_GSERVICES")
            ), parsed.permissions
        )

        assertEquals(
            listOf(
                Entry(attributes = mapOf("glEsVersion" to "0x00020000", "required" to "true")),
                Entry("android.hardware.camera", mapOf("required" to "false")),
                Entry("android.hardware.camera.autofocus", mapOf("required" to "false")),
                Entry("android.hardware.telephony", mapOf("required" to "false")),
            ), parsed.features
        )

        assertEquals(
            listOf(
                Entry("android.ext.adservices", mapOf("required" to "false")),
                Entry("androidx.window.extensions", mapOf("required" to "false")),
                Entry("androidx.window.sidecar", mapOf("required" to "false")),
                Entry("org.apache.http.legacy", mapOf("required" to "false")),
            ), parsed.libraries
        )

        assertEquals(
            listOf(
                Entry("androidx.work.impl.background.systemjob.SystemJobService", mapOf("type" to "service")),
                Entry("androidx.work.impl.diagnostics.DiagnosticsReceiver", mapOf("type" to "receiver")),
                Entry("com.testapp.onboarding.splash.SplashActivity", mapOf("type" to "activity")),
                Entry("com.testapp.payments.core.processout.ProcessOutCallbackActivity", mapOf("type" to "activity")),
             ), parsed.exports
        )
    }

}
