package geometry

/**
 * Represents a 2D point with integer coordinates.
 */
data class Point2D(
    val x: Int,
    val y: Int
) {
    companion object {
        fun new(x: Int, y: Int) = Point2D(x, y)
    }
}
