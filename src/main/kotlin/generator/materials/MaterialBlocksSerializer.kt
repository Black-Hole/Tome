package generator.materials

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import minecraft.BlockID

object MaterialBlocksSerializer : KSerializer<MaterialBlocks> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("MaterialBlocks")

    override fun deserialize(decoder: Decoder): MaterialBlocks {
        require(decoder is JsonDecoder)
        val element = decoder.decodeJsonElement()

        return when {
            element is JsonPrimitive && element.isString -> {
                // Single block as string
                MaterialBlocks.Block(BlockID.fromString(element.content))
            }
            element is JsonObject -> {
                // Map of blocks with weights
                val blocks = element.entries.associate { (key, value) ->
                    BlockID.fromString(key) to value.jsonPrimitive.float
                }
                MaterialBlocks.Blocks(blocks)
            }
            else -> throw IllegalArgumentException("Invalid MaterialBlocks format")
        }
    }

    override fun serialize(encoder: Encoder, value: MaterialBlocks) {
        require(encoder is JsonEncoder)
        val element = when (value) {
            is MaterialBlocks.Block -> JsonPrimitive(value.block.toString())
            is MaterialBlocks.Blocks -> {
                buildJsonObject {
                    value.blocks.forEach { (block, weight) ->
                        put(block.toString(), weight)
                    }
                }
            }
        }
        encoder.encodeJsonElement(element)
    }
}
