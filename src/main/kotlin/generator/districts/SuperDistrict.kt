package generator.districts

import editor.World
import geometry.Point3D

data class SuperDistrictID(val value: Int)

data class SuperDistrict(
    val id: SuperDistrictID,
    val districts: MutableSet<DistrictID> = mutableSetOf(),
    val data: DistrictData<SuperDistrictID> = DistrictData.empty()
) : HasDistrictData<SuperDistrictID>, AdjacencyAnalyzeable<SuperDistrictID> {

    override fun data(): DistrictData<SuperDistrictID> = data

    fun addDistrict(district: District, world: World) {
        districts.add(district.id)

        for (point in district.points()) {
            data.points.add(point)
            data.points2d.add(point.dropY())
            world.superDistrictMap[point.x][point.z] = id
        }

        data.isBorder = district.data.isBorder
        data.sum += district.sum()
    }

    fun addSuperdistrict(
        other: SuperDistrict,
        districts: Map<DistrictID, District>,
        world: World
    ) {
        for (districtId in other.districts) {
            val district = districts[districtId]
                ?: error("District with id ${districtId.value} not found")
            addDistrict(district, world)
        }

        val myId = id
        val otherId = other.id

        for ((neighbour, amt) in other.districtAdjacency()) {
            if (neighbour != myId) {
                data.districtAdjacency[neighbour] =
                    (data.districtAdjacency[neighbour] ?: 0) + amt
            }
        }

        data.adjacenciesCount = (data.adjacenciesCount + other.data.adjacenciesCount) -
                (data.districtAdjacency[otherId] ?: 0) -
                (other.data.districtAdjacency[myId] ?: 0)
        data.districtAdjacency.remove(otherId)
    }

    fun getAdjacencyRatio(id: SuperDistrictID): Float {
        val count = data.districtAdjacency[id] ?: 0
        return count.toFloat() / data.adjacenciesCount.toFloat()
    }

    fun getSubtypes(districts: Map<DistrictID, District>): Map<DistrictType, Int> {
        val subtypes = mutableMapOf<DistrictType, Int>()
        for (districtId in districts()) {
            val district = districts[districtId]
                ?: error("District with id ${districtId.value} not found")
            val districtType = district.data.districtType
            subtypes[districtType] = (subtypes[districtType] ?: 0) + 1
        }
        return subtypes
    }

    override fun incrementAdjacency(id: SuperDistrictID?) {
        data.adjacenciesCount += 1
        if (id != null) {
            data.districtAdjacency[id] = (data.districtAdjacency[id] ?: 0) + 1
        }
    }

    override fun addEdge(point: Point3D) {
        data.edges.add(point)
    }

    companion object {
        fun new(id: SuperDistrictID): SuperDistrict = SuperDistrict(id)
    }
}
