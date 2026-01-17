package geometry

/**
 * Represents a 3D rectangular region defined by an origin point and size.
 */
data class Rect3D(
    val origin: Point3D,
    val size: Point3D
) {
    companion object {
        /**
         * Creates a Rect3D from two points, calculating origin and size.
         */
        fun fromPoints(point1: Point3D, point2: Point3D): Rect3D {
            val origin = Point3D(
                x = minOf(point1.x, point2.x),
                y = minOf(point1.y, point2.y),
                z = minOf(point1.z, point2.z)
            )
            val size = Point3D(
                x = kotlin.math.abs(point1.x - point2.x),
                y = kotlin.math.abs(point1.y - point2.y),
                z = kotlin.math.abs(point1.z - point2.z)
            )
            return Rect3D(origin, size)
        }
    }

    /**
     * Checks if a point is contained within this rectangle.
     */
    fun contains(point: Point3D): Boolean {
        return point.x >= origin.x && point.x < origin.x + size.x &&
               point.y >= origin.y && point.y < origin.y + size.y &&
               point.z >= origin.z && point.z < origin.z + size.z
    }

    /**
     * Calculates the volume of this rectangle.
     */
    fun volume(): Int = size.x * size.y * size.z

    /**
     * Returns the length (x-dimension) of this rectangle.
     */
    fun length(): Int = size.x

    /**
     * Returns the width (z-dimension) of this rectangle.
     */
    fun width(): Int = size.z

    /**
     * Returns the height (y-dimension) of this rectangle.
     */
    fun height(): Int = size.y

    /**
     * Returns the far corner point (origin + size).
     */
    fun farPoint(): Point3D = Point3D(
        x = origin.x + size.x,
        y = origin.y + size.y,
        z = origin.z + size.z
    )

    /**
     * Projects this 3D rectangle to 2D by dropping the y-coordinate.
     */
    fun dropY(): Rect2D = Rect2D(
        origin = Point2D(origin.x, origin.z),
        size = Point2D(size.x, size.z)
    )

    /**
     * Returns the last point in the rectangle (inclusive).
     */
    fun last(): Point3D = Point3D(
        x = origin.x + size.x - 1,
        y = origin.y + size.y - 1,
        z = origin.z + size.z - 1
    )

    /**
     * Returns a sequence of all points within this rectangle in x, y, z order.
     */
    fun asSequence(): Sequence<Point3D> = sequence {
        for (z in origin.z until origin.z + size.z) {
            for (y in origin.y until origin.y + size.y) {
                for (x in origin.x until origin.x + size.x) {
                    yield(Point3D(x, y, z))
                }
            }
        }
    }
}

/**
 * Represents a 2D rectangular region defined by an origin point and size.
 */
data class Rect2D(
    val origin: Point2D,
    val size: Point2D
) {
    companion object {
        /**
         * Creates a new Rect2D with the given origin and size.
         */
        fun new(origin: Point2D, size: Point2D): Rect2D = Rect2D(origin, size)

        /**
         * Creates a Rect2D from two points, calculating origin and size.
         */
        fun fromPoints(point1: Point2D, point2: Point2D): Rect2D {
            val origin = Point2D(
                x = minOf(point1.x, point2.x),
                y = minOf(point1.y, point2.y)
            )
            val size = Point2D(
                x = kotlin.math.abs(point1.x - point2.x),
                y = kotlin.math.abs(point1.y - point2.y)
            )
            return Rect2D(origin, size)
        }
    }

    /**
     * Calculates the area of this rectangle.
     */
    fun area(): Int = size.x * size.y

    /**
     * Returns the length (x-dimension) of this rectangle.
     */
    fun length(): Int = size.x

    /**
     * Returns the width (y-dimension) of this rectangle.
     */
    fun width(): Int = size.y

    /**
     * Returns the last point in the rectangle (inclusive).
     */
    fun last(): Point2D = Point2D(
        x = origin.x + size.x - 1,
        y = origin.y + size.y - 1
    )

    /**
     * Checks if a point is on the edge of this rectangle.
     */
    fun onEdge(point: Point2D): Boolean {
        return ((point.x == origin.x || point.x == origin.x + size.x - 1) &&
               (point.y >= origin.y && point.y < origin.y + size.y)) ||
               ((point.y == origin.y || point.y == origin.y + size.y - 1) &&
               (point.x >= origin.x && point.x < origin.x + size.x))
    }

    /**
     * Checks if a point is contained within this rectangle.
     */
    fun contains(point: Point2D): Boolean {
        return point.x >= origin.x && point.x < origin.x + size.x &&
               point.y >= origin.y && point.y < origin.y + size.y
    }

    /**
     * Returns the far corner point (origin + size).
     */
    fun farPoint(): Point2D = Point2D(
        x = origin.x + size.x,
        y = origin.y + size.y
    )

    /**
     * Returns the midpoint of this rectangle.
     */
    fun midpoint(): Point2D = Point2D(
        x = origin.x + size.x / 2,
        y = origin.y + size.y / 2
    )

    /**
     * Returns a sequence of all points within this rectangle in x, y order.
     */
    fun asSequence(): Sequence<Point2D> = sequence {
        for (y in origin.y until origin.y + size.y) {
            for (x in origin.x until origin.x + size.x) {
                yield(Point2D(x, y))
            }
        }
    }
    
    /**
     * Iterates over all points in this rectangle (alias for asSequence).
     */
    fun iter(): Sequence<Point2D> = asSequence()
}
