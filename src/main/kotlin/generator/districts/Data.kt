package generator.districts

import geometry.Point2D
import geometry.Point3D

data class DistrictData<TID>(
    var origin: Point3D = Point3D(0, 0, 0),
    var isBorder: Boolean = false,
    val points: MutableSet<Point3D> = mutableSetOf(),
    val points2d: MutableSet<Point2D> = mutableSetOf(),
    val edges: MutableSet<Point3D> = mutableSetOf(),
    var sum: Point3D = Point3D(0, 0, 0),
    var districtType: DistrictType = DistrictType.Unknown,
    val districtAdjacency: MutableMap<TID, Int> = mutableMapOf(),
    var adjacenciesCount: Int = 0
) {
    companion object {
        fun <TID> empty(): DistrictData<TID> = DistrictData()

        fun <TID> new(origin: Point3D): DistrictData<TID> = DistrictData(
            origin = origin,
            sum = origin
        )
    }
}

interface HasDistrictData<TID> {
    fun data(): DistrictData<TID>

    fun origin(): Point3D = data().origin

    fun isBorder(): Boolean = data().isBorder

    fun points(): Set<Point3D> = data().points

    fun points2d(): Set<Point2D> = data().points2d

    fun sum(): Point3D = data().sum

    fun districtType(): DistrictType = data().districtType

    fun average(): Point3D {
        if (data().points.isEmpty()) {
            return Point3D(0, 0, 0)
        }
        return data().sum / data().points.size
    }

    fun size(): Int = data().points.size

    fun edges(): Set<Point3D> = data().edges

    fun districtAdjacency(): Map<TID, Int> = data().districtAdjacency

    fun adjacenciesCount(): Int = data().adjacenciesCount
}
