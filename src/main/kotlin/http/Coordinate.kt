package http

import geometry.Point3D
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

/**
 * Represents a coordinate that can be either absolute or relative.
 */
@Serializable(with = CoordinateSerializer::class)
sealed class Coordinate {
    data class Absolute(val value: Int) : Coordinate()
    data class Relative(val value: Int) : Coordinate()

    companion object {
        fun fromInt(value: Int): Coordinate = Absolute(value)
    }
}

/**
 * Custom serializer for Coordinate that handles both absolute (integer) and relative (~integer) formats.
 */
object CoordinateSerializer : KSerializer<Coordinate> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Coordinate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Coordinate) {
        when (value) {
            is Coordinate.Absolute -> encoder.encodeInt(value.value)
            is Coordinate.Relative -> encoder.encodeString("~${value.value}")
        }
    }

    override fun deserialize(decoder: Decoder): Coordinate {
        return try {
            val intValue = decoder.decodeInt()
            Coordinate.Absolute(intValue)
        } catch (e: Exception) {
            val strValue = decoder.decodeString()
            if (strValue.startsWith('~')) {
                val relativeValue = strValue.substring(1).toInt()
                Coordinate.Relative(relativeValue)
            } else {
                val absoluteValue = strValue.toInt()
                Coordinate.Absolute(absoluteValue)
            }
        }
    }
}

/**
 * Represents a 3D coordinate with x, y, z components that can be absolute or relative.
 */
@Serializable
data class Coordinate3D(
    val x: Coordinate,
    val y: Coordinate,
    val z: Coordinate
) {
    companion object {
        fun new(x: Coordinate, y: Coordinate, z: Coordinate) = Coordinate3D(x, y, z)

        fun fromPoint(point: Point3D) = Coordinate3D(
            x = Coordinate.Absolute(point.x),
            y = Coordinate.Absolute(point.y),
            z = Coordinate.Absolute(point.z)
        )
    }

    /**
     * Converts this Coordinate3D to a Point3D.
     * Throws an exception if any coordinate is relative.
     */
    fun toPoint(): Point3D {
        val xVal = when (x) {
            is Coordinate.Absolute -> x.value
            is Coordinate.Relative -> error("Relative coordinates cannot be converted to Point3D")
        }
        val yVal = when (y) {
            is Coordinate.Absolute -> y.value
            is Coordinate.Relative -> error("Relative coordinates cannot be converted to Point3D")
        }
        val zVal = when (z) {
            is Coordinate.Absolute -> z.value
            is Coordinate.Relative -> error("Relative coordinates cannot be converted to Point3D")
        }
        return Point3D(xVal, yVal, zVal)
    }
}

/**
 * Extension function to convert Int to Coordinate.
 */
fun Int.toCoordinate(): Coordinate = Coordinate.Absolute(this)

/**
 * Extension function to convert Point3D to Coordinate3D.
 */
fun Point3D.toCoordinate3D(): Coordinate3D = Coordinate3D.fromPoint(this)
