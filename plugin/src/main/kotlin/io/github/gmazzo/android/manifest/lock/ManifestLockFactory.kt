package io.github.gmazzo.android.manifest.lock

import com.charleskorn.kaml.SingleLineStringStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.encodeToStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

internal object ManifestLockFactory {

    val yaml = Yaml(
        configuration = YamlConfiguration(
            encodeDefaults = false,
            singleLineStringStyle = SingleLineStringStyle.Plain,
        ),
    )

    private val emptyManifest =
        Manifest(null, null, null, null, null, null, null)

    fun create(manifests: Map<String, Manifest>): ManifestLock {
        val main = computeMain(manifests.values.asSequence())
        val variants = computeVariants(main, manifests)
        val fingerprint = computeFingerprint(main, manifests)
        return ManifestLock(main, variants, fingerprint)
    }

    private fun computeMain(manifests: Sequence<Manifest>): Manifest {
        fun <T> sameOrNull(mapper: (Manifest) -> T?) = manifests
            .map(mapper)
            .reduce { acc, it -> if (acc == it) acc else null }

        fun onlySame(mapper: (Manifest) -> List<Manifest.Entry>?) = manifests
            .mapNotNull(mapper)
            .map { it.toSet() }
            .reduce { acc, it -> acc.intersect(it) }
            .toList()

        val namespace = sameOrNull(Manifest::namespace)
        val minSDK = sameOrNull(Manifest::minSDK)
        val targetSDK = sameOrNull(Manifest::targetSDK)
        val permissions = onlySame(Manifest::permissions)
        val features = onlySame(Manifest::features)
        val libraries = onlySame(Manifest::libraries)
        val exports = onlySame(Manifest::exports)
        return Manifest(namespace, minSDK, targetSDK, permissions, features, libraries, exports)
    }

    private fun computeVariants(main: Manifest, manifests: Map<String, Manifest>) =
        manifests.entries.asSequence().mapNotNull { (variant, m) ->
            when (val reduced = m.copy(
                namespace = m.namespace.takeIf { it != main.namespace },
                minSDK = m.minSDK.takeIf { it != main.minSDK },
                targetSDK = m.targetSDK.takeIf { it != main.targetSDK },
                permissions = m.permissions?.without(main.permissions),
                features = m.features?.without(main.features),
                libraries = m.libraries?.without(main.libraries),
                exports = m.exports?.without(main.exports),
            )) {
                emptyManifest -> null
                else -> variant to reduced
            }
        }.toMap().takeUnless { it.isEmpty() }

    private fun List<Manifest.Entry>.without(other: List<Manifest.Entry>?) = when (other) {
        null -> this
        else -> this - other.toSet()
    }.takeUnless { it.isEmpty() }

    private fun computeFingerprint(main: Manifest, variants: Map<String, Manifest>): String {
        val content = with(ByteArrayOutputStream()) {
            yaml.encodeToStream(main, this)
            yaml.encodeToStream(variants, this)
            toByteArray()
        }

        val md5 = MessageDigest.getInstance("MD5")
        return md5.digest(content).joinToString(separator = "") { "%02x".format(it) }
    }

}