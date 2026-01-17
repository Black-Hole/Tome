package http

import kotlinx.serialization.Serializable
import minecraft.Biome

/**
 * Represents a biome at a specific position in the world.
 */
@Serializable
data class PositionedBiome(
    val x: Int,
    val y: Int,
    val z: Int,
    val id: Biome
)
