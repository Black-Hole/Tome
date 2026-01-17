package generator.nbts

import geometry.Point3D
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import minecraft.Block
import minecraft.BlockID

@Serializable
data class NBTStructure(
    val size: List<Int>,
    val palette: List<PaletteBlock>,
    val blocks: List<BlockData>,
    val entities: List<Entity> = emptyList()
) {
    fun addBlock(block: Block, pos: Point3D, palette: MutableList<PaletteBlock>, blocks: MutableList<BlockData>) {
        val state = palette.indexOfFirst { it.name == block.id && it.properties == block.state }
            .takeIf { it >= 0 }
            ?: run {
                palette.add(PaletteBlock(name = block.id, properties = block.state))
                palette.size - 1
            }

        blocks.add(
            BlockData(
                state = state,
                pos = listOf(pos.x, pos.y, pos.z),
                nbt = block.data
            )
        )
    }

    companion object {
        fun fromBlocks(blocks: List<Pair<Block, Point3D>>): NBTStructure {
            val palette = mutableListOf<PaletteBlock>()
            val blockData = mutableListOf<BlockData>()

            val min = blocks.fold(Point3D(0, 0, 0)) { acc, (_, pos) ->
                Point3D(
                    x = minOf(acc.x, pos.x),
                    y = minOf(acc.y, pos.y),
                    z = minOf(acc.z, pos.z)
                )
            }
            val max = blocks.fold(Point3D(0, 0, 0)) { acc, (_, pos) ->
                Point3D(
                    x = maxOf(acc.x, pos.x),
                    y = maxOf(acc.y, pos.y),
                    z = maxOf(acc.z, pos.z)
                )
            }

            for ((block, pos) in blocks) {
                val state = palette.indexOfFirst { it.name == block.id && it.properties == block.state }
                    .takeIf { it >= 0 }
                    ?: run {
                        palette.add(PaletteBlock(name = block.id, properties = block.state))
                        palette.size - 1
                    }

                blockData.add(
                    BlockData(
                        state = state,
                        pos = listOf(pos.x, pos.y, pos.z),
                        nbt = block.data
                    )
                )
            }

            return NBTStructure(
                size = listOf(max.x - min.x + 1, max.y - min.y + 1, max.z - min.z + 1),
                palette = palette,
                blocks = blockData,
                entities = emptyList()
            )
        }
    }
}

@Serializable
data class PaletteBlock(
    @SerialName("Name")
    val name: BlockID,
    @SerialName("Properties")
    val properties: Map<String, String>? = null
)

@Serializable
data class BlockData(
    val state: Int,
    val pos: List<Int>,
    val nbt: String? = null
)

@Serializable
data class Entity(
    val pos: List<Double>,
    @SerialName("blockPos")
    val blockPos: List<Int>,
    val nbt: String // Changed from fastnbt::Value to String for simplicity
)
