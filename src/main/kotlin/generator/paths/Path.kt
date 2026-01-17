package generator.paths

import generator.materials.MaterialId
import geometry.Point3D

enum class PathPriority {
    LOW,
    MEDIUM,
    HIGH
}

data class Path(
    private val points: List<Point3D>,
    private val width: Int,
    private val material: MaterialId,
    private val priority: PathPriority
) {
    fun points(): List<Point3D> = points

    fun width(): Int = width

    fun material(): MaterialId = material

    fun priority(): PathPriority = priority
}
