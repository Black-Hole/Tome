package minecraft

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
enum class BlockForm {
    @SerialName("block")
    BLOCK,
    @SerialName("stairs")
    STAIRS,
    @SerialName("slab")
    SLAB,
    @SerialName("wall")
    WALL,
    @SerialName("fence")
    FENCE,
    @SerialName("fence_gate")
    FENCE_GATE,
    @SerialName("pillar")
    PILLAR,
    @SerialName("trapdoor")
    TRAPDOOR,
    @SerialName("door")
    DOOR,
    @SerialName("button")
    BUTTON,
    @SerialName("pressure_plate")
    PRESSURE_PLATE,
    @SerialName("chiseled")
    CHISELED,
    @SerialName("wood")
    WOOD,
    @SerialName("log")
    LOG,
    
    // SIGNS
    @SerialName("sign")
    SIGN,
    @SerialName("wall_sign")
    WALL_SIGN,
    @SerialName("hanging_sign")
    HANGING_SIGN,
    @SerialName("hanging_wall_sign")
    HANGING_WALL_SIGN,
    
    // DECORATION
    @SerialName("flower")
    FLOWER,
    
    // SPARSE
    @SerialName("sparse")
    SPARSE;
    
    companion object {
        /**
         * Infers the block form from a BlockID by examining its serialized name.
         */
        fun inferFromBlock(block: BlockID): BlockForm {
            val idString = Json.encodeToString(block)
            
            return when {
                idString.contains("stairs") -> STAIRS
                idString.contains("slab") -> SLAB
                idString.contains("wall") && !idString.contains("wall_sign") -> WALL
                idString.contains("fence_gate") -> FENCE_GATE
                idString.contains("fence") -> FENCE
                idString.contains("pillar") || idString.contains("log") -> PILLAR
                idString.contains("trapdoor") -> TRAPDOOR
                idString.contains("door") -> DOOR
                idString.contains("button") -> BUTTON
                idString.contains("pressure_plate") -> PRESSURE_PLATE
                idString.contains("chiseled") -> CHISELED
                idString.contains("hanging_wall_sign") -> HANGING_WALL_SIGN
                idString.contains("hanging_sign") -> HANGING_SIGN
                idString.contains("wall_sign") -> WALL_SIGN
                idString.contains("sign") -> SIGN
                idString.contains("air") || idString.contains("water") || 
                    idString.contains("lava") || idString.contains("snow") -> SPARSE
                else -> BLOCK
            }
        }
    }
    
    /**
     * Returns the density value for this block form (0.0 to 1.0).
     */
    fun density(): Float = when (this) {
        BLOCK -> 1.0f
        STAIRS -> 0.5f
        SLAB -> 0.4f
        WALL -> 0.5f
        FENCE -> 0.4f
        FENCE_GATE -> 0.4f
        PILLAR -> 1.0f
        TRAPDOOR -> 0.2f
        DOOR -> 0.2f
        BUTTON -> 0.1f
        PRESSURE_PLATE -> 0.1f
        CHISELED -> 1.0f
        SIGN, WALL_SIGN, HANGING_SIGN, HANGING_WALL_SIGN -> 0.1f
        SPARSE -> 0.0f
        WOOD -> 1.0f
        LOG -> 1.0f
        FLOWER -> 0.0f
    }
}
