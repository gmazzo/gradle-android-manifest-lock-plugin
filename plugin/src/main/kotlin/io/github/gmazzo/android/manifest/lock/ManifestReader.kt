package io.github.gmazzo.android.manifest.lock

import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.util.TreeMap
import javax.xml.namespace.NamespaceContext
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathFactory

internal object ManifestReader {
    private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"

    private val docBuilder = DocumentBuilderFactory.newInstance()
        .apply { isNamespaceAware = true }
        .newDocumentBuilder()

    private val xPath = XPathFactory.newInstance().newXPath().apply { namespaceContext = AndroidNamespaceContext }
    private val getPackageName = xPath.compile("/manifest/@package")
    private val getMinSDK = xPath.compile("/manifest/uses-sdk/@android:minSdkVersion")
    private val getTargetSDK = xPath.compile("/manifest/uses-sdk/@android:targetSdkVersion")
    private val getPermissions = xPath.compile("/manifest/uses-permission")
    private val getFeatures = xPath.compile("/manifest/uses-feature")
    private val getLibraries = xPath.compile("/manifest/application/uses-library")
    private val getExports = xPath.compile("//*[@android:exported='true']")

    fun parse(manifest: File, variantName: String? = null): Manifest {
        val source = docBuilder.parse(manifest)
        val packageName = getPackageName.evaluate(source).takeUnless { it.isBlank() }
        val minSDK = getMinSDK.evaluate(source).toIntOrNull()
        val targetSDK = getTargetSDK.evaluate(source).toIntOrNull()
        val permissions = getPermissions.collect(source)
        val features = getFeatures.collect(source)
        val libraries = getLibraries.collect(source)
        val exports = getExports
            .collect(source) { "${it.localName}#${it.androidName}" }
            .toSortedSet()

        return Manifest(
            variantName,
            packageName,
            minSDK,
            targetSDK,
            permissions,
            features,
            libraries,
            exports
        )
    }

    private fun <Item> XPathExpression.collect(
        source: Any,
        mapper: (Node) -> Item
    ): Sequence<Item> {
        val items = evaluate(source, XPathConstants.NODESET) as NodeList
        return (0 until items.length).asSequence().map(items::item).map(mapper)
    }

    private fun XPathExpression.collect(source: Any) = TreeMap<String, MutableMap<String, String>>().apply {
        collect(source) {
            val name = it.androidName.orEmpty()
            val values = linkedMapOf<String, String>()
            (0 until it.attributes.length).forEach { j ->
                val attr = it.attributes.item(j)

                if (attr.namespaceURI == ANDROID_NS && attr.localName != "name") {
                    values[attr.localName] = attr.nodeValue
                }
            }
            name to values
        }.forEach { (name, attrs) ->
            put(name, attrs)?.putAll(attrs)
        }
    }

    private val Node.androidName
        get() = attributes.getNamedItemNS(ANDROID_NS, "name")?.nodeValue

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
