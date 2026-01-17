package http

import kotlinx.serialization.Serializable
import minecraft.Block
import minecraft.BlockID

/**
 * Represents a block positioned in the world with coordinates and optional state/data.
 */
@Serializable
data class PositionedBlock(
    val id: BlockID,
    val x: Coordinate,
    val y: Coordinate,
    val z: Coordinate,
    val state: Map<String, String>? = null,
    val data: String? = null
) {
    companion object {
        /**
         * Creates a PositionedBlock from a Block and position.
         */
        fun fromBlock(block: Block, position: Coordinate3D) = PositionedBlock(
            id = block.id,
            x = position.x,
            y = position.y,
            z = position.z,
            state = block.state,
            data = block.data
        )
    }

    /**
     * Gets the coordinate of this positioned block.
     */
    fun getCoordinate(): Coordinate3D = Coordinate3D.new(x, y, z)

    /**
     * Converts this positioned block to a Block.
     */
    fun getBlock(): Block = Block(
        id = id,
        state = state,
        data = data
    )
}

/**
 * Response from block placement operations.
 */
@Serializable
data class BlockPlacementResponse(
    val status: Int
)
