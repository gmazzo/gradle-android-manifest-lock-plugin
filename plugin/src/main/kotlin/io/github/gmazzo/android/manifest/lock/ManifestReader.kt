package io.github.gmazzo.android.manifest.lock

import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.namespace.NamespaceContext
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathFactory

internal object ManifestReader {
    private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"

    private val docBuilderFactory = DocumentBuilderFactory.newInstance()
        .apply { isNamespaceAware = true }

    private val xPath = XPathFactory.newInstance().newXPath().apply { namespaceContext = AndroidNamespaceContext }
    private val getPackageName = xPath.compile("/manifest/@package")
    private val getMinSDK = xPath.compile("/manifest/uses-sdk/@android:minSdkVersion")
    private val getTargetSDK = xPath.compile("/manifest/uses-sdk/@android:targetSdkVersion")
    private val getPermissions = xPath.compile("/manifest/uses-permission")
    private val getFeatures = xPath.compile("/manifest/uses-feature")
    private val getLibraries = xPath.compile("/manifest/application/uses-library")
    private val getExports = xPath.compile("//*[@android:exported='true']")

    fun parse(manifest: File): Manifest {
        val source = docBuilderFactory.newDocumentBuilder().parse(manifest)
        val packageName = getPackageName.evaluate(source).takeUnless { it.isBlank() }
        val minSDK = getMinSDK.evaluate(source).toIntOrNull()
        val targetSDK = getTargetSDK.evaluate(source).toIntOrNull()
        val permissions = getPermissions.collectEntries(source)
        val features = getFeatures.collectEntries(source)
        val libraries = getLibraries.collectEntries(source)
        val exports = getExports.collect(source)
            .groupingBy { it.nodeName }
            .fold(emptySet<String>()) { acc, node -> acc + node.attributes.getNamedItemNS(ANDROID_NS, "name").nodeValue }

        return Manifest(packageName, minSDK, targetSDK, permissions, features, libraries, exports)
    }

    private fun XPathExpression.collect(source: Any): Sequence<Node> {
        val items = evaluate(source, XPathConstants.NODESET) as NodeList
        return (0 until items.length).asSequence().map(items::item)
    }

    private fun XPathExpression.collectEntries(source: Any): List<Manifest.Entry>? = collect(source)
        .map { node ->
            val attrs = (0 until node.attributes.length).asSequence()
                .map(node.attributes::item)
                .filter { it.namespaceURI == ANDROID_NS }
                .map { it.localName to it.nodeValue }
                .toMap(linkedMapOf())
            val name = attrs.remove("name")

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
