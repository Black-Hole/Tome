package http

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Types of height maps available in Minecraft.
 */
@Serializable
enum class HeightMapType {
    @SerialName("WORLD_SURFACE")
    WORLD_SURFACE,
    
    @SerialName("OCEAN_FLOOR_NO_PLANTS")
    OCEAN_FLOOR_NO_PLANTS,
    
    @SerialName("MOTION_BLOCKING_NO_LEAVES")
    MOTION_BLOCKING_NO_PLANTS,
    
    @SerialName("MOTION_BLOCKING")
    MOTION_BLOCKING;

    override fun toString(): String = when (this) {
        WORLD_SURFACE -> "WORLD_SURFACE"
        OCEAN_FLOOR_NO_PLANTS -> "OCEAN_FLOOR_NO_PLANTS"
        MOTION_BLOCKING_NO_PLANTS -> "MOTION_BLOCKING_NO_LEAVES"
        MOTION_BLOCKING -> "MOTION_BLOCKING"
    }
}
