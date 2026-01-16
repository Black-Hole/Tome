package generator.districts

import editor.Editor
import editor.World
import geometry.Point2D
import geometry.Point3D
import geometry.Rect2D
import noise.RNG
import noise.Seed
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Districts")

data class DistrictID(val value: Int)

enum class DistrictType {
    Unknown,
    Urban,
    Rural,
    OffLimits
}

data class District(
    val id: DistrictID,
    val data: DistrictData<DistrictID> = DistrictData.new(Point3D(0, 0, 0))
) : HasDistrictData<DistrictID>, AdjacencyAnalyzeable<DistrictID> {

    override fun data(): DistrictData<DistrictID> = data

    private fun addPoint(point: Point3D) {
        data.points.add(point)
        data.points2d.add(point.dropY())
        data.sum = sum() + point
    }

    private fun setToBorderDistrict() {
        data.isBorder = true
    }

    fun getAdjacencyRatio(id: DistrictID): Float {
        val count = data.districtAdjacency[id] ?: 0
        return count.toFloat() / data.adjacenciesCount.toFloat()
    }

    override fun incrementAdjacency(id: DistrictID?) {
        data.adjacenciesCount += 1
        if (id != null) {
            data.districtAdjacency[id] = (data.districtAdjacency[id] ?: 0) + 1
        }
    }

    override fun addEdge(point: Point3D) {
        data.edges.add(point)
    }

    companion object {
        fun spawnDistricts(seed: Seed, world: World): List<District> {
            val rng = RNG.fromSeedAndString(seed, "spawn_districts")
            val rects = mutableListOf<Rect2D>()

            for (i in 0 until (world.buildArea.size.x / CHUNK_SIZE) * (world.buildArea.size.z / CHUNK_SIZE)) {
                val x = i % (world.buildArea.size.x / CHUNK_SIZE)
                val z = i / (world.buildArea.size.x / CHUNK_SIZE)
                val rect = Rect2D(
                    Point2D(x * CHUNK_SIZE, z * CHUNK_SIZE),
                    Point2D(CHUNK_SIZE, CHUNK_SIZE)
                )
                rects.add(rect)
            }

            val points = mutableListOf<Point3D>()

            for (rect in rects) {
                var trials = 0

                while (trials < SPAWN_DISTRICTS_RETRIES) {
                    trials++

                    val trialPoint = world.addHeight(rng.randPoint2D(rect.size) + rect.origin)

                    if (points.all { p ->
                            p.distanceSquared(trialPoint) > SPAWN_DISTRICTS_MIN_DISTANCE * SPAWN_DISTRICTS_MIN_DISTANCE
                        }) {
                        points.add(trialPoint)
                        break
                    }
                }
            }

            return points.mapIndexed { idx, p ->
                District(
                    id = DistrictID(idx),
                    data = DistrictData.new(p)
                )
            }
        }

        fun bubbleOut(districts: MutableMap<DistrictID, District>, world: World) {
            val queue = districts.values.map { it.data.origin }.toMutableList()
            val visited = queue.toMutableSet()

            while (queue.isNotEmpty()) {
                val next = queue.removeAt(0)

                logger.info("Bubbling out from $next")
                val currentDistrict = world.districtMap[next.x][next.z]
                    ?: error("Every explored tile should have a district")
                logger.info("Current district: $currentDistrict")

                for (neighbour in Point3D.CARDINALS.map { it + next }) {
                    if (neighbour in visited) {
                        continue
                    }

                    if (!world.buildArea.contains(world.buildArea.origin.withoutY() + neighbour)) {
                        logger.info("Skipping $neighbour because it is out of bounds")
                        districts[currentDistrict]?.setToBorderDistrict()
                        continue
                    }

                    visited.add(neighbour)
                    queue.add(neighbour)
                    world.districtMap[neighbour.x][neighbour.z] = currentDistrict
                    districts[currentDistrict]?.addPoint(neighbour)
                }
            }
        }

        fun recenterDistricts(world: World, districts: MutableMap<DistrictID, District>) {
            val sizeX = world.buildArea.size.x
            val sizeZ = world.buildArea.size.z
            world.districtMap.clear()
            for (i in 0 until sizeX) {
                world.districtMap.add(MutableList<DistrictID?>(sizeZ) { null })
            }

            for (district in districts.values) {
                district.data.origin = world.addHeight(district.average().dropY())
                district.data.points.clear()
                district.data.points2d.clear()
                district.data.sum = Point3D(0, 0, 0)
                district.data.isBorder = false
                district.addPoint(district.data.origin)

                world.districtMap[district.data.origin.x][district.data.origin.z] = district.id
            }

            bubbleOut(districts, world)
        }
    }
}

suspend fun generateDistricts(seed: Seed, editor: Editor) {
    logger.info("Generating districts with seed: $seed")

    val districts = District.spawnDistricts(seed, editor.world())

    for (district in districts) {
        val x = district.data.origin.x
        val z = district.data.origin.z
        editor.world().districtMap[x][z] = district.id
    }

    val districtMap = districts.associateBy { it.id }.toMutableMap()

    logger.info("Bubbling out districts...")
    District.bubbleOut(districtMap, editor.world())

    logger.info("Re-centering districts...")
    repeat(NUM_RECENTER) {
        District.recenterDistricts(editor.world(), districtMap)
    }

    logger.info("Analyzing adjacency of districts...")
    analyzeAdjacency(
        districtMap,
        editor.world().getHeightMap(),
        editor.world().districtMap,
        editor.world().worldRect2d(),
        false
    )

    logger.info("Creating superdistricts...")
    var superDistrictIdCounter = 0
    val superDistricts = mutableMapOf<SuperDistrictID, SuperDistrict>()
    val districtAnalysisData = mutableMapOf<DistrictID, DistrictAnalysis>()

    for (district in districtMap.values) {
        logger.info("Analyzing district ${district.id.value}")
        val analysis = analyzeDistrict(district.data(), editor)
        districtAnalysisData[district.id] = analysis
    }

    for (district in districtMap.values) {
        val id = SuperDistrictID(superDistrictIdCounter++)
        val superDistrict = SuperDistrict.new(id)
        superDistrict.addDistrict(district, editor.world())
        superDistricts[superDistrict.id] = superDistrict
    }

    val superdistrictAnalysisData = mutableMapOf<SuperDistrictID, DistrictAnalysis>()
    for (district in superDistricts.values) {
        logger.info("Analyzing district ${district.id.value}")
        superdistrictAnalysisData[district.id] = analyzeDistrict(district.data(), editor)
    }

    classifyDistricts(districtMap, districtAnalysisData)

    analyzeAdjacency(
        superDistricts,
        editor.world().getHeightMap(),
        editor.world().superDistrictMap,
        editor.world().worldRect2d(),
        true
    )

    logger.info("Merging down superdistricts...")
    mergeDown(superDistricts, districtMap, superdistrictAnalysisData, editor)

    analyzeAdjacency(
        superDistricts,
        editor.world().getHeightMap(),
        editor.world().superDistrictMap,
        editor.world().worldRect2d(),
        false
    )

    editor.world().districts.clear()
    editor.world().districts.putAll(districtMap)
    editor.world().superDistricts.clear()
    editor.world().superDistricts.putAll(superDistricts)

    classifySuperdistricts(
        editor.world().superDistricts,
        editor.world().districts,
        superdistrictAnalysisData
    )
    logger.info("Districts generated successfully")
}
