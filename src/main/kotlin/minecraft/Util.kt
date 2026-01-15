package minecraft

import geometry.Point3D

/**
 * Converts a world point to chunk coordinates by dividing by 16.
 */
fun pointToChunkCoordinates(point: Point3D): Point3D {
    return Point3D(
        x = point.x / 16,
        y = point.y / 16,
        z = point.z / 16
    )
}
