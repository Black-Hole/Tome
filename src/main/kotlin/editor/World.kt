package editor

import generator.districts.DistrictID
import generator.districts.SuperDistrictID
import generator.districts.District
import generator.districts.SuperDistrict
import generator.districts.DistrictType
import generator.BuildClaim
import geometry.Point2D
import geometry.Point3D
import geometry.Rect2D
import geometry.Rect3D
import http.GDMCHTTPProvider
import http.HeightMapType
import minecraft.*
import org.slf4j.LoggerFactory

private const val CHUNK_SIZE = 16

class World(
    val buildArea: Rect3D,
    val districts: MutableMap<DistrictID, District> = mutableMapOf(),
    val superDistricts: MutableMap<SuperDistrictID, SuperDistrict> = mutableMapOf(),
    val districtMap: MutableList<MutableList<DistrictID?>> = mutableListOf(),
    val superDistrictMap: MutableList<MutableList<SuperDistrictID?>> = mutableListOf(),
    val buildings: MutableList<generator.buildings.BuildingData> = mutableListOf(),
    
    private val groundHeightMap: MutableList<MutableList<Int>>,
    private val groundBlockMap: MutableList<MutableList<Block>>,
    private val oceanFloorHeightMap: MutableList<MutableList<Int>>,
    private val groundBiomeMap: MutableList<MutableList<Biome>>,
    private val motionBlockingHeightMap: MutableList<MutableList<Int>>,
    private val buildClaimMap: MutableList<MutableList<BuildClaim>>,
    val chunks: Map<Point2D, Chunk>
) {
    private val logger = LoggerFactory.getLogger(World::class.java)
    
    companion object {
        suspend fun new(provider: GDMCHTTPProvider): World {
            val logger = LoggerFactory.getLogger(World::class.java)
            val buildArea = provider.getBuildArea().getOrThrow()
            val (originX, originZ, sizeX, sizeZ) = listOf(
                buildArea.origin.x,
                buildArea.origin.z,
                buildArea.size.x,
                buildArea.size.z
            )
            val (sizeXUsize, sizeZUsize) = sizeX to sizeZ

            val districtMap = MutableList(sizeXUsize) { MutableList<DistrictID?>(sizeZUsize) { null } }
            val superDistrictMap = MutableList(sizeXUsize) { MutableList<SuperDistrictID?>(sizeZUsize) { null } }
            
            val chunkRect = Rect3D(
                origin = buildArea.origin / CHUNK_SIZE,
                size = buildArea.last() / CHUNK_SIZE - buildArea.origin / CHUNK_SIZE + Point3D(1, 1, 1)
            )

            logger.info("Loading chunks...")
            val chunks = provider.getChunks(
                chunkRect.origin.x,
                chunkRect.origin.y,
                chunkRect.origin.z,
                chunkRect.size.x,
                chunkRect.size.y,
                chunkRect.size.z
            ).getOrThrow()
                .associateBy { Point2D(it.xPos, it.zPos) }

            logger.info("Loading heightmaps...")
            val groundMap = provider.getHeightmap(
                originX, originZ, sizeX, sizeZ,
                HeightMapType.MOTION_BLOCKING_NO_PLANTS
            ).getOrThrow()
            val oceanMap = provider.getHeightmap(
                originX, originZ, sizeX, sizeZ,
                HeightMapType.OCEAN_FLOOR_NO_PLANTS
            ).getOrThrow()
            val motionBlockingMap = provider.getHeightmap(
                originX, originZ, sizeX, sizeZ,
                HeightMapType.MOTION_BLOCKING
            ).getOrThrow()

            val groundHeightMap = MutableList(sizeXUsize) { MutableList(sizeZUsize) { 0 } }
            val oceanFloorHeightMap = MutableList(sizeXUsize) { MutableList(sizeZUsize) { 0 } }
            val motionBlockingHeightMap = MutableList(sizeXUsize) { MutableList(sizeZUsize) { 0 } }
            val groundBlockMap = MutableList(sizeXUsize) { 
                MutableList(sizeZUsize) { Block(BlockID.UNKNOWN, null, null) } 
            }
            val buildClaimMap = MutableList(sizeXUsize) { MutableList(sizeZUsize) { BuildClaim.None } }
            val groundBiomeMap = MutableList(sizeXUsize) { MutableList(sizeZUsize) { Biome.UNKNOWN } }

            val world = World(
                buildArea = buildArea,
                groundHeightMap = groundHeightMap,
                oceanFloorHeightMap = oceanFloorHeightMap,
                motionBlockingHeightMap = motionBlockingHeightMap,
                buildClaimMap = buildClaimMap,
                chunks = chunks,
                groundBiomeMap = groundBiomeMap,
                groundBlockMap = groundBlockMap,
                districtMap = districtMap,
                superDistrictMap = superDistrictMap
            )

            val yOffset = buildArea.origin.y
            for (x in 0 until sizeXUsize) {
                for (z in 0 until sizeZUsize) {
                    world.groundHeightMap[x][z] = groundMap[x][z] - yOffset
                    world.oceanFloorHeightMap[x][z] = oceanMap[x][z] - yOffset
                    world.motionBlockingHeightMap[x][z] = motionBlockingMap[x][z] - yOffset
                    world.groundBlockMap[x][z] = world.getBlock(
                        Point3D(x, world.groundHeightMap[x][z], z)
                    ) ?: Block(BlockID.UNKNOWN, null, null)
                    world.groundBiomeMap[x][z] = world.getBiome(
                        Point3D(x, world.oceanFloorHeightMap[x][z], z)
                    ) ?: Biome.UNKNOWN
                }
            }

            return world
        }
    }

    fun getEditor(): Editor = Editor(buildArea, this)

    fun origin(): Point3D = buildArea.origin

    fun size(): Point3D = buildArea.size

    fun worldRect2d(): Rect2D = Rect2D(
        origin = Point2D(0, 0),
        size = Point2D(buildArea.size.x, buildArea.size.z)
    )

    fun iterPoints2d(): Sequence<Point2D> = Rect2D(
        origin = Point2D(0, 0),
        size = Point2D(buildArea.size.x, buildArea.size.z)
    ).asSequence()

    fun getHeightAt(point: Point2D): Int = groundHeightMap[point.x][point.y]

    fun getHeightMap(): List<List<Int>> = groundHeightMap

    fun getOceanFloorHeightAt(point: Point2D): Int = oceanFloorHeightMap[point.x][point.y]

    fun getMotionBlockingHeightAt(point: Point2D): Int = motionBlockingHeightMap[point.x][point.y]

    fun getSurfaceBiomeAt(point: Point2D): Biome {
        val height = getOceanFloorHeightAt(point)
        val point3d = Point3D(point.x, height, point.y)
        return getBiome(point3d) ?: Biome.UNKNOWN
    }

    fun getDistrictAt(point: Point2D): DistrictID? = districtMap[point.x][point.y]

    fun getSuperDistrictAt(point: Point2D): SuperDistrictID? = superDistrictMap[point.x][point.y]

    fun addHeight(point: Point2D): Point3D = Point3D(point.x, getHeightAt(point), point.y)

    fun isInBounds2d(point: Point2D): Boolean = 
        buildArea.dropY().contains(point + buildArea.origin.dropY())

    fun getBlock(point: Point3D): Block? {
        val adjustedPoint = point + buildArea.origin
        logger.info("Getting block at point: {}", adjustedPoint)

        val chunkCoordinates = pointToChunkCoordinates(adjustedPoint)
        val chunk = chunks[chunkCoordinates.dropY()] ?: return null

        val section = chunk.sections.find { it.y == adjustedPoint.y / CHUNK_SIZE } ?: return null
        val blockStates = section.blockStates ?: return null

        if (blockStates.data == null) {
            val block = blockStates.palette.getOrNull(0) ?: return null
            return Block(
                id = BlockID.fromString(block.name),
                state = block.properties,
                data = null
            )
        }

        val data = blockStates.data
        val blockIndex = getDataIndex(data, adjustedPoint) ?: return null
        val palette = blockStates.palette

        return palette.getOrNull(blockIndex)?.let { block ->
            Block(
                id = BlockID.fromString(block.name),
                state = block.properties,
                data = null
            )
        }
    }

    fun getBiome(point: Point3D): Biome? {
        val adjustedPoint = point + buildArea.origin
        val chunkCoordinates = pointToChunkCoordinates(adjustedPoint)
        val chunk = chunks[chunkCoordinates.dropY()] ?: return null
        val section = chunk.sections.find { it.y == adjustedPoint.y / CHUNK_SIZE } ?: return null
        val biomes = section.biomes ?: return null

        if (biomes.data == null) {
            return biomes.biomes.getOrNull(0)
        }

        val data = biomes.data
        val biomeIndex = getDataIndex(data, adjustedPoint) ?: return null
        val biomeList = biomes.biomes

        return biomeList.getOrNull(biomeIndex)
    }

    private fun getDataIndex(data: LongArray, point: Point3D): Int? {
        val index = (point.x.mod(CHUNK_SIZE) + 
                    point.z.mod(CHUNK_SIZE) * CHUNK_SIZE + 
                    point.y.mod(CHUNK_SIZE) * CHUNK_SIZE * CHUNK_SIZE)

        val indicesPerLong = kotlin.math.ceil((CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE).toFloat() / data.size.toFloat()).toInt()
        val bits = 64 / indicesPerLong
        val longIndex = index / indicesPerLong
        val bitIndex = index % indicesPerLong

        val long = data.getOrNull(longIndex) ?: return null
        val blockIndex = (long shr (bitIndex * bits)) and ((1L shl bits) - 1)

        return blockIndex.toInt()
    }

    fun isWater(point: Point2D): Boolean = groundBlockMap[point.x][point.y].id == BlockID.WATER

    fun isWater3d(point: Point3D): Boolean = getBlock(point)?.id == BlockID.WATER

    fun isClaimed(point: Point2D): Boolean = buildClaimMap[point.x][point.y] != BuildClaim.None

    fun claim(point: Point2D, claim: BuildClaim) {
        if (isInBounds2d(point)) {
            buildClaimMap[point.x][point.y] = claim
        } else {
            logger.warn("Tried to claim point {} out of bounds", point)
        }
    }

    fun getUrbanPoints(): Set<Point2D> {
        return iterPoints2d()
            .filter { point -> getDistrictType(point) == DistrictType.Urban }
            .toSet()
    }

    fun getDistrictType(point: Point2D): DistrictType? {
        return getSuperDistrictAt(point)?.let { districtId ->
            superDistricts[districtId]?.data?.districtType
        }
    }
}
