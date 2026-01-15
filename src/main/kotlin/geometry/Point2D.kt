package geometry

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Represents a 2D point with integer coordinates.
 */
data class Point2D(
    val x: Int,
    val y: Int
) {
    companion object {
        fun new(x: Int, y: Int) = Point2D(x, y)

        // Direction constants
        val NORTH = Point2D(0, -1)
        val SOUTH = Point2D(0, 1)
        val EAST = Point2D(1, 0)
        val WEST = Point2D(-1, 0)
        
        val NORTHEAST = Point2D(1, -1)
        val NORTHWEST = Point2D(-1, -1)
        val SOUTHEAST = Point2D(1, 1)
        val SOUTHWEST = Point2D(-1, 1)

        val CARDINALS = arrayOf(NORTH, SOUTH, EAST, WEST)
        val ALL_8 = arrayOf(NORTH, SOUTH, EAST, WEST, NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST)
    }

    /**
     * Calculates the Euclidean distance to another point.
     */
    fun distance(other: Point2D): Double {
        val dx = (x - other.x).toDouble().pow(2)
        val dy = (y - other.y).toDouble().pow(2)
        return sqrt(dx + dy)
    }

    /**
     * Calculates the squared distance to another point (avoids sqrt).
     */
    fun distanceSquared(other: Point2D): Int {
        val dx = x - other.x
        val dy = y - other.y
        return dx * dx + dy * dy
    }

    /**
     * Returns the four cardinal neighbors of this point.
     */
    fun neighbours(): List<Point2D> = CARDINALS.map { this + it }

    /**
     * Adds another point to this point.
     */
    operator fun plus(other: Point2D): Point2D = Point2D(x + other.x, y + other.y)

    /**
     * Subtracts another point from this point.
     */
    operator fun minus(other: Point2D): Point2D = Point2D(x - other.x, y - other.y)

    /**
     * Multiplies this point by a scalar.
     */
    operator fun times(scalar: Int): Point2D = Point2D(x * scalar, y * scalar)

    /**
     * Divides this point by a scalar.
     */
    operator fun div(scalar: Int): Point2D = Point2D(x / scalar, y / scalar)

    /**
     * Negates this point.
     */
    operator fun unaryMinus(): Point2D = Point2D(-x, -y)
}
