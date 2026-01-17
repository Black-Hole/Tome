package generator.nbts

import geometry.Cardinal
import geometry.Point3D
import minecraft.Block

/**
 * Rotation is always in the clockwise direction
 */
enum class Rotation {
    NONE,
    ONCE,
    TWICE,
    THRICE;

    fun applyToPoint(point: Point3D): Point3D {
        return when (this) {
            NONE -> point
            ONCE -> Point3D(point.z, point.y, -point.x)
            TWICE -> Point3D(-point.x, point.y, -point.z)
            THRICE -> Point3D(-point.z, point.y, point.x)
        }
    }

    fun applyToCardinal(cardinal: Cardinal): Cardinal {
        return when (this) {
            NONE -> cardinal
            ONCE -> when (cardinal) {
                Cardinal.NORTH -> Cardinal.EAST
                Cardinal.EAST -> Cardinal.SOUTH
                Cardinal.SOUTH -> Cardinal.WEST
                Cardinal.WEST -> Cardinal.NORTH
            }
            TWICE -> when (cardinal) {
                Cardinal.NORTH -> Cardinal.SOUTH
                Cardinal.EAST -> Cardinal.WEST
                Cardinal.SOUTH -> Cardinal.NORTH
                Cardinal.WEST -> Cardinal.EAST
            }
            THRICE -> when (cardinal) {
                Cardinal.NORTH -> Cardinal.WEST
                Cardinal.EAST -> Cardinal.NORTH
                Cardinal.SOUTH -> Cardinal.EAST
                Cardinal.WEST -> Cardinal.SOUTH
            }
        }
    }

    fun applyToBlock(block: Block): Block {
        if (block.state == null) {
            return block // No state to apply rotation to
        }

        val newState = block.state.toMutableMap()
        val keys = newState.keys.toList()
        
        for (key in keys) {
            val value = newState[key] ?: continue
            
            // Check if the value is a cardinal direction
            val cardinal = Cardinal.fromString(value)
            if (cardinal != null) {
                val newCardinal = applyToCardinal(cardinal)
                newState[key] = newCardinal.toString()
            } else if (key == "axis" && (this == ONCE || this == THRICE)) {
                // Rotate axis property
                newState[key] = when (value) {
                    "x" -> "z"
                    "y" -> "y"
                    "z" -> "x"
                    else -> value // Keep the same if not x, y, or z
                }
            } else if (key == "rotation") {
                // Rotate rotation property (for skulls, etc.)
                val rotation = value.toIntOrNull() ?: 0
                val newRotation = (rotation + when (this) {
                    NONE -> 0
                    ONCE -> 4
                    TWICE -> 8
                    THRICE -> 12
                }).mod(16)
                newState[key] = newRotation.toString()
            }
        }

        return block.copy(state = newState)
    }

    operator fun plus(other: Rotation): Rotation {
        return fromInt(toInt() + other.toInt())
    }

    operator fun minus(other: Rotation): Rotation {
        return fromInt(toInt() - other.toInt())
    }

    operator fun unaryMinus(): Rotation {
        return when (this) {
            NONE -> NONE
            ONCE -> THRICE
            TWICE -> TWICE
            THRICE -> ONCE
        }
    }

    fun toInt(): Int {
        return when (this) {
            NONE -> 0
            ONCE -> 1
            TWICE -> 2
            THRICE -> 3
        }
    }

    companion object {
        fun fromInt(value: Int): Rotation {
            return when (value.mod(4)) {
                0 -> NONE
                1 -> ONCE
                2 -> TWICE
                3 -> THRICE
                else -> NONE // Should never happen
            }
        }

        fun fromCardinal(cardinal: Cardinal): Rotation {
            return when (cardinal) {
                Cardinal.NORTH -> NONE
                Cardinal.EAST -> ONCE
                Cardinal.SOUTH -> TWICE
                Cardinal.WEST -> THRICE
            }
        }
    }
}
