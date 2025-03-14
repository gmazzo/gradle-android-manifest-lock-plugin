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

    private fun computeMain(manifests: Sequence<Manifest>) = Manifest(
        namespace = manifests.mapNotNull(Manifest::namespace).sameOrNull(),
        minSDK = manifests.mapNotNull(Manifest::minSDK).sameOrNull(),
        targetSDK = manifests.mapNotNull(Manifest::targetSDK).sameOrNull(),
        configurations = manifests.mapNotNull(Manifest::configurations).onlySame(),
        permissions = manifests.mapNotNull(Manifest::permissions).onlySame(),
        features = manifests.mapNotNull(Manifest::features).onlySame(),
        libraries = manifests.mapNotNull(Manifest::libraries).onlySame(),
        exports = manifests.mapNotNull(Manifest::exports)
            .reduceOrNull { acc, it ->
                acc.mapValues { (type, names) ->
                    names.intersect(it[type].orEmpty())
                }
            }
            ?.withoutEmpties(),
        nativeLibraries = manifests.mapNotNull(Manifest::nativeLibraries).onlySame(),
    )

    private fun <Type> Sequence<Type?>.sameOrNull() = this
        .reduceOrNull { acc, it -> if (acc == it) acc else null }

    private fun <Type> Sequence<Iterable<Type>>.onlySame() = this
        .map { it.toSet() }
        .reduceOrNull { acc, it -> acc.intersect(it) }
        ?.toList()
        ?.takeUnless { it.isEmpty() }

    private fun computeVariants(main: Manifest, manifests: Map<String, Manifest>) =
        manifests.entries.asSequence().mapNotNull { (variant, m) ->
            when (val reduced = m.copy(
                namespace = m.namespace.takeIf { it != main.namespace },
                minSDK = m.minSDK.takeIf { it != main.minSDK },
                targetSDK = m.targetSDK.takeIf { it != main.targetSDK },
                configurations = m.configurations?.without(main.configurations),
                permissions = m.permissions?.without(main.permissions),
                features = m.features?.without(main.features),
                libraries = m.libraries?.without(main.libraries),
                exports = m.exports.orEmpty()
                    .mapValues { (type, names) -> names - main.exports?.get(type).orEmpty() }
                    .withoutEmpties(),
                nativeLibraries = m.nativeLibraries?.without(main.nativeLibraries),
            )) {
                emptyManifest -> null
                else -> variant to reduced
            }
        }.toMap().takeUnless { it.isEmpty() }

    private fun <Type> List<Type>.without(other: List<Type>?) = when (other) {
        null -> this
        else -> this - other.toSet()
    }.takeUnless { it.isEmpty() }

    private fun Map<String, Set<String>>.withoutEmpties() = this
        .filterValues { it.isNotEmpty() }
        .takeUnless { it.isEmpty() }

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
