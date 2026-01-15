package http

import geometry.Point3D
import geometry.Rect3D
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from the build area endpoint.
 */
@Serializable
data class BuildAreaResponse(
    @SerialName("xFrom")
    val xFrom: Int,
    @SerialName("yFrom")
    val yFrom: Int,
    @SerialName("zFrom")
    val zFrom: Int,
    @SerialName("xTo")
    val xTo: Int,
    @SerialName("yTo")
    val yTo: Int,
    @SerialName("zTo")
    val zTo: Int
) {
    /**
     * Converts this build area response to a Rect3D.
     */
    fun toRect(): Rect3D = Rect3D.fromPoints(
        Point3D.new(xFrom, yFrom, zFrom),
        Point3D.new(xTo, yTo, zTo)
    )
}
