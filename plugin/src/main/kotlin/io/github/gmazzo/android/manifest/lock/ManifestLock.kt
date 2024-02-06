package io.github.gmazzo.android.manifest.lock

import io.github.gmazzo.android.manifest.lock.ManifestLockFactory.yaml
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import org.gradle.kotlin.dsl.provideDelegate

@Serializable
internal data class ManifestLock(
    val main: Manifest,
    val variants: Map<String, Manifest>? = null,
    val fingerprint: String,
) {
    val content: String by lazy { yaml.encodeToString(this) + "\n" }
}
