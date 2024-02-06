package io.github.gmazzo.android.manifest.lock

import kotlinx.serialization.Serializable

@Serializable
internal data class Manifest(
    val namespace: String? = null,
    val minSDK: Int? = null,
    val targetSDK: Int? = null,
    val permissions: List<Entry>? = null,
    val features: List<Entry>? = null,
    val libraries: List<Entry>? = null,
    val exports: List<Entry>? = null,
) {

    @Serializable(with = ManifestEntrySerializer::class)
    data class Entry(val name: String? = null, val attributes: Map<String, String>? = null)

}
