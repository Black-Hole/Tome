package generator.nbts

import geometry.Point3D

data class Transform(
    var position: Point3D,
    var rotation: Rotation
) {
    fun apply(point: Point3D): Point3D {
        return when (rotation) {
            Rotation.NONE -> point + position
            Rotation.ONCE -> Point3D(point.z, point.y, -point.x) + position
            Rotation.TWICE -> Point3D(-point.x, point.y, -point.z) + position
            Rotation.THRICE -> Point3D(-point.z, point.y, point.x) + position
        }
    }

    fun shift(offset: Point3D) {
        position += offset
    }

    fun rotate(amount: Int) {
        val current = rotation.toInt()
        rotation = Rotation.fromInt(current + amount)
    }

    companion object {
        fun new(position: Point3D, rotation: Rotation): Transform {
            return Transform(position, rotation)
        }

        fun fromPoint(position: Point3D): Transform {
            return Transform(position, Rotation.NONE)
        }

        fun fromRotation(rotation: Rotation): Transform {
            return Transform(Point3D(0, 0, 0), rotation)
        }
    }
}
