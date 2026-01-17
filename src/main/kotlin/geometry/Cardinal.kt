package geometry

/**
 * Represents the four cardinal directions.
 */
enum class Cardinal {
    NORTH,
    EAST,
    SOUTH,
    WEST;

    companion object {
        /**
         * Creates a Cardinal from a Point3D if it matches a cardinal direction.
         */
        fun fromPoint(point: Point3D): Cardinal? {
            return when {
                point.x == 0 && point.y == 0 && point.z == -1 -> NORTH
                point.x == 1 && point.y == 0 && point.z == 0 -> EAST
                point.x == 0 && point.y == 0 && point.z == 1 -> SOUTH
                point.x == -1 && point.y == 0 && point.z == 0 -> WEST
                else -> null
            }
        }

        /**
         * Creates a Cardinal from a Point2D if it matches a cardinal direction.
         */
        fun fromPoint2D(point: Point2D): Cardinal? {
            return when {
                point.x == 0 && point.y == -1 -> NORTH
                point.x == 1 && point.y == 0 -> EAST
                point.x == 0 && point.y == 1 -> SOUTH
                point.x == -1 && point.y == 0 -> WEST
                else -> null
            }
        }

        /**
         * Creates a Cardinal from a string representation.
         * Accepts "north", "east", "south", "west" and aliases "z_minus", "x_plus", "z_plus", "x_minus".
         */
        fun fromString(s: String): Cardinal? {
            return when (s.lowercase()) {
                "north", "z_minus" -> NORTH
                "east", "x_plus" -> EAST
                "south", "z_plus" -> SOUTH
                "west", "x_minus" -> WEST
                else -> null
            }
        }
    }

    /**
     * Converts this cardinal to a Point3D vector.
     */
    fun toPoint3D(): Point3D = when (this) {
        NORTH -> Point3D(0, 0, -1)
        EAST -> Point3D(1, 0, 0)
        SOUTH -> Point3D(0, 0, 1)
        WEST -> Point3D(-1, 0, 0)
    }

    /**
     * Converts this cardinal to a Point2D vector.
     */
    fun toPoint2D(): Point2D = when (this) {
        NORTH -> Point2D(0, -1)
        EAST -> Point2D(1, 0)
        SOUTH -> Point2D(0, 1)
        WEST -> Point2D(-1, 0)
    }

    /**
     * Converts this cardinal to a string representation.
     */
    override fun toString(): String = when (this) {
        NORTH -> "north"
        EAST -> "east"
        SOUTH -> "south"
        WEST -> "west"
    }

    /**
     * Rotates this cardinal 90 degrees clockwise.
     */
    fun rotateRight(): Cardinal = when (this) {
        NORTH -> EAST
        EAST -> SOUTH
        SOUTH -> WEST
        WEST -> NORTH
    }

    /**
     * Rotates this cardinal 90 degrees counter-clockwise.
     */
    fun rotateLeft(): Cardinal = when (this) {
        NORTH -> WEST
        EAST -> NORTH
        SOUTH -> EAST
        WEST -> SOUTH
    }

    /**
     * Returns the opposite cardinal direction.
     */
    fun opposite(): Cardinal = when (this) {
        NORTH -> SOUTH
        EAST -> WEST
        SOUTH -> NORTH
        WEST -> EAST
    }

    /**
     * Unary minus operator returns the opposite cardinal direction.
     */
    operator fun unaryMinus(): Cardinal = opposite()
}
