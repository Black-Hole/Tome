package generator.buildings

import data.Point3DSerializer
import geometry.Cardinal
import geometry.Point3D
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StairPlacement(
    @Serializable(with = Point3DSerializer::class)
    val cell: Point3D,
    val direction: Cardinal,
    @SerialName("left_to_right")
    val leftToRight: Boolean = false
)
