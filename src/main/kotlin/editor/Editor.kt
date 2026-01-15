package editor

import data.Loadable
import geometry.Point3D
import geometry.Rect3D
import http.GDMCHTTPProvider
import http.PositionedBlock
import http.toCoordinate3D
import kotlinx.serialization.Serializable
import minecraft.Block
import minecraft.BlockForm
import minecraft.BlockID
import noise.RNG
import org.slf4j.LoggerFactory

@Serializable
data class Material(
    val id: String
)

object MaterialLoader : Loadable<Material, String> {
    override fun getKey(item: Material): String = item.id
    override fun path(): String = "materials"
    override fun deserializer() = Material.serializer()
}

class Editor(
    private val buildArea: Rect3D,
    private val world: World
) : AutoCloseable {
    private val logger = LoggerFactory.getLogger(Editor::class.java)
    
    private val provider = GDMCHTTPProvider()
    private val blockBuffer = mutableListOf<PositionedBlock>()
    private var bufferSize = 32
    private val blockCache = mutableMapOf<Point3D, Block>()
    private val materials: Map<String, Material>
    private val blockFormCache = mutableMapOf<BlockID, BlockForm>()

    init {
        logger.info("Loading editor data")
        materials = MaterialLoader.load()
    }

    fun setBufferSize(size: Int) {
        bufferSize = size
    }

    suspend fun placeBlock(block: Block, point: Point3D) {
        placeBlockOptions(block, point, force = false)
    }

    suspend fun placeBlockForced(block: Block, point: Point3D) {
        placeBlockOptions(block, point, force = true)
    }

    suspend fun placeBlockOptions(block: Block, point: Point3D, force: Boolean) {
        if (!world.buildArea.contains(point + buildArea.origin)) {
            logger.warn("Point {} is outside the build area {} and will be ignored", point + buildArea.origin, world.buildArea)
            return
        }

        if (block.id == BlockID.UNKNOWN) {
            logger.warn("Attempted to place an unknown block at {}, skipping", point)
            return
        }

        if (!force && blockCache.containsKey(point)) {
            val currentBlock = blockCache[point]!!.id
            
            if (getBlockForm(block.id).density() <= getBlockForm(currentBlock).density()) {
                logger.info("Block at {} is already placed with a denser block, skipping", point)
                return
            }
        }

        blockCache[point] = block
        blockBuffer.add(PositionedBlock.fromBlock(block, (point + buildArea.origin).toCoordinate3D()))
        
        if (blockBuffer.size >= bufferSize) {
            flushBuffer()
        }
    }

    private fun getBlockForm(id: BlockID): BlockForm {
        if (!blockFormCache.containsKey(id)) {
            val form = BlockForm.inferFromBlock(id)
            blockFormCache[id] = form
        }
        return blockFormCache[id]!!
    }

    suspend fun placeBlockChance(block: Block, point: Point3D, rng: RNG, chance: Int) {
        if (rng.randI32Range(1, 100) <= chance) {
            placeBlock(block, point)
        }
    }

    fun getBlock(point: Point3D): Block {
        blockCache[point - buildArea.origin]?.let { return it }
        
        return world.getBlock(point)
            ?: error("Block at $point not found in world")
    }

    suspend fun flushBuffer() {
        val result = provider.putBlocks(blockBuffer).getOrThrow()
        
        result.forEachIndexed { index, response ->
            val point = blockBuffer[index].getCoordinate().toPoint()
            val block = blockBuffer[index].getBlock()
            
            if (response.status == 0 && world.getBlock(point)?.let { it != block } != false) {
                if (block.id == BlockID.AIR && world.getBlock(point) == null) {
                    return@forEachIndexed
                }

                if (blockCache.containsKey(point - buildArea.origin) && getBlock(point) == block) {
                    return@forEachIndexed
                }
                
                logger.error("Failed to place block {} at {}, world block is {}", block, point, world.getBlock(point))
            }
        }
        
        blockBuffer.clear()
    }

    fun world(): World = world
    
    fun worldMut(): World = world

    override fun close() {
        if (blockBuffer.isNotEmpty()) {
            logger.error("Editor was dropped with non-empty block buffer!")
        }
    }
}
