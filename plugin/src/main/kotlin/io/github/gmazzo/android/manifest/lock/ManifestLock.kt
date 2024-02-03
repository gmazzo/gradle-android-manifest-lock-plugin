package io.github.gmazzo.android.manifest.lock

import java.security.MessageDigest

internal class ManifestLock(
    private val manifests: List<Manifest>,
) {

    constructor(vararg manifests: Manifest) : this(manifests.toList())

    val main by lazy(::computeMain)

    val content by lazy(::computeContent)

    private fun computeMain(): Manifest {
        val seq = manifests.asSequence()

        fun <T> Sequence<T?>.sameOrNull() =
            reduce { acc, it -> if (acc == it) acc else null }

        fun <T> Sequence<Set<T>>.onlySame() =
            reduce { acc, it -> acc.intersect(it) }

        fun <K, V> Sequence<Map<K, V>>.onlySame() =
            map { it.entries }.onlySame().associate { it.key to it.value }

        val namespace = seq.map(Manifest::namespace).sameOrNull()
        val minSDK = seq.map(Manifest::minSDK).sameOrNull()
        val targetSDK = seq.map(Manifest::targetSDK).sameOrNull()
        val permissions = seq.map(Manifest::permissions).onlySame()
        val features = seq.map(Manifest::features).onlySame()
        val libraries = seq.map(Manifest::libraries).onlySame()
        val exports = seq.map(Manifest::exports).onlySame()
        return Manifest(variantName = null, namespace, minSDK, targetSDK, permissions, features, libraries, exports)
    }

    private fun computeContent() = buildString {
        appendContent(main, null)
        manifests.forEach { m ->
            if (m.copy(variantName = null) != main) {
                appendLine()
                append("VARIANT: ")
                appendLine(m.variantName)
                appendContent(m, main)
            }
        }
        appendFingerprint()
    }

    private fun StringBuilder.appendContent(manifest: Manifest, main: Manifest?) {
        val tab = main != null

        if (manifest.namespace != main?.namespace) {
            appendTab(tab)
            append("NAMESPACE: ")
            appendLine(manifest.namespace)
        }
        if (manifest.minSDK != main?.minSDK || manifest.targetSDK != main?.targetSDK) {
            appendTab(tab)
            appendLine("SDKS: ")
            if (manifest.minSDK != main?.minSDK) {
                appendTab(tab)
                append("  min=")
                appendLine(manifest.minSDK)
            }
            if (manifest.targetSDK != main?.targetSDK) {
                appendTab(tab)
                append("  target=")
                appendLine(manifest.targetSDK)
            }
        }
        if (manifest.permissions != main?.permissions && manifest.permissions.isNotEmpty()) {
            appendTab(tab)
            appendLine("PERMISSIONS:")
            appendContent(tab, manifest.permissions, main?.permissions)
        }
        if (manifest.features != main?.features && manifest.features.isNotEmpty()) {
            appendTab(tab)
            appendLine("FEATURES:")
            appendContent(tab, manifest.features, main?.features)
        }
        if (manifest.libraries != main?.libraries && manifest.libraries.isNotEmpty()) {
            appendTab(tab)
            appendLine("LIBRARIES:")
            appendContent(tab, manifest.libraries, main?.libraries)
        }
        if (manifest.exports != main?.exports && manifest.exports.isNotEmpty()) {
            appendTab(tab)
            appendLine("EXPORTS:")
            manifest.exports.forEach {
                if (main == null || it !in main.exports) {
                    appendTab(tab)
                    append("  ")
                    appendLine(it)
                }
            }
        }
    }

    private fun StringBuilder.appendContent(
        tab: Boolean,
        entries: Map<String, Map<String, String>>,
        main: Map<String, Map<String, String>>?
    ) = entries.forEach { (name, attrs) ->
        if (main == null || attrs != main[name]) {
            appendContent(tab, name, attrs)
        }
    }

    private fun StringBuilder.appendContent(tab: Boolean, name: String, attrs: Map<String, String>) {
        appendTab(tab)
        append("  ")
        append(name)

        var first = true
        attrs.forEach { (attr, value) ->
            append(if (first) if (name.isBlank()) "" else ": " else ", ")
            first = false
            append(attr)
            append('=')
            append(value)
        }
        appendLine()
    }

    private fun StringBuilder.appendTab(tab: Boolean) {
        if (tab) append("  ")
    }

    private fun StringBuilder.appendFingerprint() {
        val md5 = MessageDigest.getInstance("MD5")
        val hash = md5.digest(toString().toByteArray())

        appendLine()
        append("FINGERPRINT: ")
        hash.forEach { append("%02x".format(it)) }
        appendLine()
    }

}
