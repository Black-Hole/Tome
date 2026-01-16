package generator.districts

import geometry.Point2D
import geometry.Point3D
import geometry.Rect2D

interface AdjacencyAnalyzeable<TID> {
    fun incrementAdjacency(id: TID?)
    fun addEdge(point: Point3D)
}

fun <TID> analyzeAdjacency(
    objects: MutableMap<TID, out AdjacencyAnalyzeable<TID>>,
    heightMap: List<List<Int>>,
    map: List<List<TID?>>,
    worldRect: Rect2D,
    ignoreEdgeAddition: Boolean
) {
    for (point in worldRect.iter()) {
        val id = map[point.x][point.y] ?: continue

        var isEdge = false
        val height = heightMap[point.x][point.y]

        for (neighbourPoint in listOf(
            point + Point2D.EAST,
            point + Point2D.SOUTH,
            point - Point2D.EAST,
            point - Point2D.SOUTH
        )) {
            if (!worldRect.contains(neighbourPoint)) {
                continue
            }

            val neighbourDistrictId = map[neighbourPoint.x][neighbourPoint.y]

            if (neighbourDistrictId == null) {
                objects[id]?.incrementAdjacency(null)
                isEdge = true
                continue
            }

            if (neighbourDistrictId == id) {
                continue
            }

            isEdge = true

            val neighbourHeight = heightMap[neighbourPoint.x][neighbourPoint.y]

            if ((neighbourHeight - height).let { if (it < 0) -it else it } > 1) {
                continue
            }

            objects[id]?.incrementAdjacency(neighbourDistrictId)
            objects[neighbourDistrictId]?.incrementAdjacency(id)
        }

        if (isEdge && !ignoreEdgeAddition) {
            val item = objects[id]
            item?.addEdge(Point3D(point.x, height, point.y))
        }
    }
}
