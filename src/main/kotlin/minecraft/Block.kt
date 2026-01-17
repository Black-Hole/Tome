package minecraft

import kotlinx.serialization.Serializable

@Serializable
data class Block(
    val id: BlockID,
    val state: Map<String, String>? = null,
    val data: String? = null
) {
    companion object {
        /**
         * Creates a Block from a BlockID.
         */
        fun from(id: BlockID): Block = Block(id = id)
    }
}

/**
 * Converts a string representation of a block into a Block struct.
 * Format: "minecraft:block_name" or "minecraft:block_name[property=value,...]"
 */
fun stringToBlock(blockStr: String): Block? {
    if (blockStr.contains('[')) {
        val parts = blockStr.split('[', limit = 2)
        val id = parts[0].toBlockID()
        val stateStr = parts.getOrNull(1)?.trimEnd(']') ?: return Block(id = id)
        
        val state = stateStr.split(',')
            .mapNotNull { pair ->
                val kv = pair.split('=', limit = 2)
                if (kv.size == 2) {
                    kv[0].trim() to kv[1].trim()
                } else {
                    null
                }
            }
            .toMap()
            .takeIf { it.isNotEmpty() }
        
        return Block(id = id, state = state)
    } else {
        return Block(id = blockStr.toBlockID())
    }
}

/**
 * Converts a string to a BlockID.
 */
private fun String.toBlockID(): BlockID {
    return BlockID.fromString(this)
}
