package io.github.gmazzo.android.manifest.lock

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Manifest.Entry::class)
internal object ManifestEntrySerializer {

    private val nameSerializer = String.serializer()

    private val valueSerializer = SingleOrMultipleSerializer(String.serializer())

    private val attrsSerializer = MapSerializer(String.serializer(), valueSerializer)

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

    @Serializer(forClass = List::class)
    class SingleOrMultipleSerializer<Type>(
        private val singleSerializer: KSerializer<Type>,
    ) : KSerializer<Collection<Type>> {

        private val multipleSerializer = ListSerializer(singleSerializer)

        override fun serialize(encoder: Encoder, value: Collection<Type>) {
            when (value.size) {
                0 -> encoder.encodeNull()
                1 -> singleSerializer.serialize(encoder, value.single())
                else -> multipleSerializer.serialize(encoder, value.toList())
            }
        }

    }

}
