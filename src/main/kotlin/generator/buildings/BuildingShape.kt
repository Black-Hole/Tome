package generator.buildings

import data.Point3DSerializer
import geometry.Point2D
import geometry.Point3D
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class BuildingShape(
    val cells: List<@Contextual Point3D>,
    val stairs: List<StairPlacement>? = null
) {
    fun getFootprint(grid: Grid): Set<Point2D> {
        return cells.flatMap { cell ->
            grid.getCellRect2D(cell).asSequence().toList()
        }.toSet()
    }

    fun cells(): List<Point3D> = cells

    fun stairs(): List<StairPlacement>? = stairs
}
