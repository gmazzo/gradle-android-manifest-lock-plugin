package io.github.gmazzo.android.manifest.lock

internal data class Manifest(
    val variantName: String?,
    val namespace: String?,
    val minSDK: Int?,
    val targetSDK: Int?,
    val permissions: Map<String, Map<String, String>>,
    val features: Map<String, Map<String, String>>,
    val libraries: Map<String, Map<String, String>>,
    val exports: Set<String>,
)
