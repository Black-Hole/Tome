package geometry

/**
 * Represents a 3D point with integer coordinates.
 */
data class Point3D(
    val x: Int,
    val y: Int,
    val z: Int
) {
    companion object {
        fun new(x: Int, y: Int, z: Int) = Point3D(x, y, z)
    }
}
