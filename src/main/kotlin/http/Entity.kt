package http

import kotlinx.serialization.Serializable

/**
 * Response from entity query operations.
 */
@Serializable
data class EntityResponse(
    val uuid: String,
    val data: String
)

/**
 * Represents an entity positioned in the world.
 */
@Serializable
data class PositionedEntity(
    val x: Coordinate,
    val y: Coordinate,
    val z: Coordinate,
    val id: String,
    val data: String? = null
)
