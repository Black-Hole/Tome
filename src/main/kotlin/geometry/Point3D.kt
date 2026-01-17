package geometry

import kotlin.math.pow
import kotlin.math.sqrt

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

        // Direction constants
        val UP = Point3D(0, 1, 0)
        val DOWN = Point3D(0, -1, 0)
        val NORTH = Point3D(0, 0, -1)
        val SOUTH = Point3D(0, 0, 1)
        val EAST = Point3D(1, 0, 0)
        val WEST = Point3D(-1, 0, 0)

        val CARDINALS = arrayOf(NORTH, SOUTH, EAST, WEST)
        val ORTHOGONALS = arrayOf(UP, DOWN, NORTH, SOUTH, EAST, WEST)
    }

    /**
     * Calculates the Euclidean distance to another point.
     */
    fun distance(other: Point3D): Double {
        val dx = (x - other.x).toDouble().pow(2)
        val dy = (y - other.y).toDouble().pow(2)
        val dz = (z - other.z).toDouble().pow(2)
        return sqrt(dx + dy + dz)
    }

    /**
     * Calculates the squared distance to another point (avoids sqrt).
     */
    fun distanceSquared(other: Point3D): Int {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return dx * dx + dy * dy + dz * dz
    }

    /**
     * Projects this point to 2D by dropping the y coordinate.
     */
    fun dropY(): Point2D = Point2D(x, z)

    /**
     * Returns a copy with y set to 0.
     */
    fun withoutY(): Point3D = Point3D(x, 0, z)

    /**
     * Returns all six orthogonal neighbors (including vertical).
     */
    fun neighbours3D(): List<Point3D> = ORTHOGONALS.map { this + it }

    /**
     * Returns the four cardinal neighbors (horizontal only).
     */
    fun neighbours2D(): List<Point3D> = CARDINALS.map { this + it }

    /**
     * Rotates this point 90 degrees counter-clockwise around the y-axis.
     */
    fun rotateLeft(): Point3D = Point3D(-z, y, x)

    /**
     * Rotates this point 90 degrees clockwise around the y-axis.
     */
    fun rotateRight(): Point3D = Point3D(z, y, -x)

    /**
     * Adds another point to this point.
     */
    operator fun plus(other: Point3D): Point3D = Point3D(x + other.x, y + other.y, z + other.z)

    /**
     * Subtracts another point from this point.
     */
    operator fun minus(other: Point3D): Point3D = Point3D(x - other.x, y - other.y, z - other.z)

    /**
     * Multiplies this point by a scalar.
     */
    operator fun times(scalar: Int): Point3D = Point3D(x * scalar, y * scalar, z * scalar)

    /**
     * Divides this point by a scalar.
     */
    operator fun div(scalar: Int): Point3D = Point3D(x / scalar, y / scalar, z / scalar)

    /**
     * Negates this point.
     */
    operator fun unaryMinus(): Point3D = Point3D(-x, -y, -z)
}
