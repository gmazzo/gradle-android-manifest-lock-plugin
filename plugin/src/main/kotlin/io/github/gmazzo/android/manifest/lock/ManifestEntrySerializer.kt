package io.github.gmazzo.android.manifest.lock

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Manifest.Entry::class)
internal object ManifestEntrySerializer {

    private val nameSerializer = String.serializer()

    private val attrsSerializer = MapSerializer(String.serializer(), String.serializer())

    private val complexSerializer = MapSerializer(String.serializer(), attrsSerializer)

    override fun serialize(encoder: Encoder, value: Manifest.Entry) {
        when (value.name) {
            null -> value.attributes?.let { attrsSerializer.serialize(encoder, it) }
            else -> when (value.attributes.isNullOrEmpty()) {
                true -> nameSerializer.serialize(encoder, value.name)
                else -> complexSerializer.serialize(encoder, mapOf(value.name to value.attributes))
            }
        }
    }

}
