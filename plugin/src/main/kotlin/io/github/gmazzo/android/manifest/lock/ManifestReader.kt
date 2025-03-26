package io.github.gmazzo.android.manifest.lock

import java.io.File
import java.util.TreeMap
import java.util.TreeSet
import javax.xml.namespace.NamespaceContext
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathFactory
import org.w3c.dom.Node
import org.w3c.dom.NodeList

internal object ManifestReader {
    private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"

    private val docBuilderFactory = DocumentBuilderFactory.newInstance()
        .apply { isNamespaceAware = true }

    private val xPath =
        XPathFactory.newInstance().newXPath().apply { namespaceContext = AndroidNamespaceContext }
    private val getPackageName = xPath.compile("/manifest/@package")
    private val getMinSDK = xPath.compile("/manifest/uses-sdk/@android:minSdkVersion")
    private val getTargetSDK = xPath.compile("/manifest/uses-sdk/@android:targetSdkVersion")
    private val getConfigurations = xPath.compile("/manifest/uses-configuration")
    private val getPermissions =
        xPath.compile("/manifest/*[self::uses-permission or self::uses-permission-sdk-23]")
    private val getFeatures = xPath.compile("/manifest/uses-feature")
    private val getLibraries = xPath.compile("/manifest/application/uses-library")
    private val getNativeLibraries = xPath.compile("/manifest/application/uses-native-library")
    private val getExports = xPath.compile("//*[@android:exported='true']")

    fun parse(
        manifest: File,
        readSDKVersion: Boolean = true,
        readConfigurations: Boolean = true,
        readPermissions: Boolean = true,
        readFeatures: Boolean = true,
        readLibraries: Boolean = true,
        readNativeLibraries: Boolean = true,
        readExports: Boolean = true
    ) = docBuilderFactory.newDocumentBuilder().parse(manifest).let { source ->
        Manifest(
            namespace = getPackageName.evaluate(source).takeUnless { it.isBlank() },
            minSDK = if (readSDKVersion) getMinSDK.evaluate(source).toIntOrNull() else null,
            targetSDK = if (readSDKVersion) getTargetSDK.evaluate(source).toIntOrNull() else null,
            configurations = if (readConfigurations) getConfigurations.collectEntries(source) else null,
            permissions = if (readPermissions) getPermissions.collectEntries(source) else null,
            features = if (readFeatures) getFeatures.collectEntries(source) else null,
            libraries = if (readLibraries) getLibraries.collectEntries(source) else null,
            nativeLibraries = if (readNativeLibraries) getNativeLibraries
                .collectEntries(source, linkedMapOf("requiredBy" to setOf("manifest"))) else null,
            exports = if (readExports) getExports
                .collect(source)
                .groupingBy { it.nodeName }
                .aggregateTo(TreeMap<String, TreeSet<String>>()) { _, acc, node, _ ->
                    val set = acc ?: TreeSet<String>()
                    set += node.attributes.getNamedItemNS(ANDROID_NS, "name").nodeValue
                    set
                }
                .toSortedMap() else null
        )
    }

    private fun XPathExpression.collect(source: Any): Sequence<Node> {
        val items = evaluate(source, XPathConstants.NODESET) as NodeList
        return (0 until items.length).asSequence().map(items::item)
    }

    private fun XPathExpression.collectEntries(
        source: Any,
        extraAttrs: MutableMap<String, Set<String>>? = null,
    ): List<Manifest.Entry>? = collect(source)
        .map { node ->
            val name = node.attributes.getNamedItemNS(ANDROID_NS, "name")?.nodeValue
            val attrs = (0 until node.attributes.length).asSequence()
                .map(node.attributes::item)
                .filter { it.namespaceURI == ANDROID_NS && it.localName != "name" }
                .map { it.localName to setOf(it.nodeValue) }
                .toMap(extraAttrs ?: linkedMapOf())

            Manifest.Entry(name, attrs.takeUnless { it.isEmpty() })
        }
        .toList()
        .sortedBy { it.name }
        .takeUnless { it.isEmpty() }

    private object AndroidNamespaceContext : NamespaceContext {

        override fun getNamespaceURI(prefix: String?) = when (prefix) {
            "android" -> ANDROID_NS
            else -> null
        }

        override fun getPrefix(namespaceURI: String?) = when (namespaceURI) {
            ANDROID_NS -> "android"
            else -> null
        }

        override fun getPrefixes(namespaceURI: String?) =
            throw UnsupportedOperationException()

    }

}
