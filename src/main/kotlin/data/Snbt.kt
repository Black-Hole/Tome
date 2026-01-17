package data

import net.kyori.adventure.nbt.*

/**
 * Utilities for converting NBT data to SNBT (Stringified NBT) format.
 *
 * SNBT is a human-readable text representation of NBT data used in Minecraft.
 */
object Snbt {
    
    /**
     * Convert an NBT binary tag to SNBT string format.
     *
     * @param tag The NBT tag to convert
     * @return The SNBT string representation
     */
    fun toSnbt(tag: BinaryTag): String = when (tag) {
        is ByteBinaryTag -> "${tag.value()}b"
        is ShortBinaryTag -> "${tag.value()}s"
        is IntBinaryTag -> tag.value().toString()
        is LongBinaryTag -> "${tag.value()}L"
        is FloatBinaryTag -> "${tag.value()}f"
        is DoubleBinaryTag -> "${tag.value()}d"
        is StringBinaryTag -> "\"${tag.value()}\""
        
        is ByteArrayBinaryTag -> buildString {
            append("[B;")
            tag.value().forEachIndexed { index, byte ->
                if (index > 0) append(',')
                append("${byte}b")
            }
            append(']')
        }
        
        is IntArrayBinaryTag -> buildString {
            append("[I;")
            tag.value().forEachIndexed { index, int ->
                if (index > 0) append(',')
                append(int)
            }
            append(']')
        }
        
        is LongArrayBinaryTag -> buildString {
            append("[L;")
            tag.value().forEachIndexed { index, long ->
                if (index > 0) append(',')
                append("${long}L")
            }
            append(']')
        }
        
        is ListBinaryTag -> buildString {
            append('[')
            tag.forEachIndexed { index, value ->
                if (index > 0) append(',')
                append(toSnbt(value))
            }
            append(']')
        }
        
        is CompoundBinaryTag -> buildString {
            append('{')
            var first = true
            tag.forEach { (key, value) ->
                if (!first) append(',')
                first = false
                
                // Quote keys that contain special characters
                if (key.contains(' ') || key.contains(':') || 
                    key.contains('{') || key.contains('}')) {
                    append('"').append(key).append('"')
                } else {
                    append(key)
                }
                
                append(':')
                append(toSnbt(value))
            }
            append('}')
        }
        
        else -> tag.toString()
    }
}
