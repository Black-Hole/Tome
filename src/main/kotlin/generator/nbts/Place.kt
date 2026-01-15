package generator.nbts

import editor.Editor
import generator.materials.Material
import generator.materials.MaterialId
import generator.materials.Palette
import generator.materials.PaletteId
import generator.materials.PaletteSwapResult
import generator.materials.Placer
import geometry.Cardinal
import geometry.Point3D
import kotlinx.serialization.json.Json
import minecraft.Block
import minecraft.BlockID
import net.kyori.adventure.nbt.BinaryTagIO
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.ListBinaryTag
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.util.zip.GZIPInputStream

private val logger = LoggerFactory.getLogger("generator.nbts.place")

suspend fun placeStructure(
    editor: Editor,
    placer: Placer?,
    structure: Structure,
    offset: Point3D,
    direction: Cardinal,
    materials: Map<MaterialId, Material>?,
    palettes: Map<PaletteId, Palette>?,
    palette: PaletteId?,
    mirrorX: Boolean = false,
    mirrorZ: Boolean = false
) {
    val rotation = Rotation.fromCardinal(structure.facing) - Rotation.fromCardinal(direction)

    val transform = when (rotation) {
        Rotation.NONE -> Transform.fromPoint(offset)
        Rotation.ONCE -> Transform.new(offset, Rotation.ONCE)
        Rotation.TWICE -> Transform.new(offset, Rotation.TWICE)
        Rotation.THRICE -> Transform.new(offset, Rotation.THRICE)
    }

    // Shift the transform to account for the structure's origin
    transform.shift(rotation.applyToPoint(-structure.origin))

    val inputPalette = structure.palette

    placeNBT(
        meta = structure.meta,
        transform = transform,
        editor = editor,
        placer = placer,
        materials = materials,
        palettes = palettes,
        inputPalette = inputPalette,
        outputPalette = palette,
        mirrorX = if (mirrorX) structure.origin.x else null,
        mirrorZ = if (mirrorZ) structure.origin.z else null
    )
}

suspend fun placeNBT(
    meta: NBTMeta,
    transform: Transform,
    editor: Editor,
    placer: Placer?,
    materials: Map<MaterialId, Material>?,
    palettes: Map<PaletteId, Palette>?,
    inputPalette: PaletteId?,
    outputPalette: PaletteId?,
    mirrorX: Int?,
    mirrorZ: Int?
) {
    logger.info("Placing NBT structure: {}", meta.path)

    val file = File(meta.path)
    val nbtData = readNBTFile(file)

    if (inputPalette == null && outputPalette == null) {
        // Place blocks directly without palette swapping
        for (blockData in nbtData.blocks) {
            val paletteData = nbtData.palette[blockData.state]
            var data = blockData.nbt

            if (data == "\"{}\"") {
                data = null
            }

            if (paletteData.name == BlockID.AIR) {
                continue // Skip air blocks
            }

            var pos = Point3D(blockData.pos[0], blockData.pos[1], blockData.pos[2])

            mirrorX?.let {
                pos = Point3D(it * 2 - pos.x, pos.y, pos.z)
            }
            mirrorZ?.let {
                pos = Point3D(pos.x, pos.y, it * 2 - pos.z)
            }

            // Apply rotation to block
            val block = (-transform.rotation).applyToBlock(
                Block(
                    id = paletteData.name,
                    state = paletteData.properties,
                    data = data
                )
            )
            editor.placeBlock(block, transform.apply(pos))
        }
    } else {
        // Place blocks with palette swapping
        requireNotNull(placer) { "Placer is required when using palettes" }
        requireNotNull(materials) { "Materials are required when using palettes" }
        requireNotNull(palettes) { "Palettes are required when using palettes" }

        val inputPal = palettes[inputPalette]
            ?: error("Palette $inputPalette not found")
        val outputPal = palettes[outputPalette]
            ?: error("Palette $outputPalette not found")

        for (blockData in nbtData.blocks) {
            val paletteData = nbtData.palette[blockData.state]
            var data = blockData.nbt

            if (data == "\"{}\"") {
                data = null
            }

            if (paletteData.name == BlockID.AIR) {
                continue // Skip air blocks
            }

            var pos = Point3D(blockData.pos[0], blockData.pos[1], blockData.pos[2])

            mirrorX?.let {
                pos = Point3D(it * 2 - pos.x, pos.y, pos.z)
            }
            mirrorZ?.let {
                pos = Point3D(pos.x, pos.y, it * 2 - pos.z)
            }

            val swap = inputPal.swapWith(paletteData.name, outputPal, materials)

            when (swap) {
                is PaletteSwapResult.Block -> {
                    val block = (-transform.rotation).applyToBlock(
                        Block(
                            id = swap.blockId,
                            state = paletteData.properties,
                            data = data
                        )
                    )
                    editor.placeBlock(block, transform.apply(pos))
                }
                is PaletteSwapResult.Material -> {
                    val block = (-transform.rotation).applyToBlock(
                        Block(
                            id = BlockID.UNKNOWN,
                            state = paletteData.properties,
                            data = data
                        )
                    )

                    placer.placeBlock(
                        editor = editor,
                        point = transform.apply(pos),
                        material = swap.materialId,
                        form = swap.form,
                        state = block.state,
                        data = block.data
                    )
                }
            }
        }
    }
}

private fun readNBTFile(file: File): NBTStructure {
    // Try to read as plain NBT first
    return try {
        FileInputStream(file).use { fis ->
            val tag = BinaryTagIO.reader().read(fis)
            parseNBTStructure(tag)
        }
    } catch (e: Exception) {
        // Try with GZIP decompression
        try {
            GZIPInputStream(FileInputStream(file)).use { gzis ->
                val tag = BinaryTagIO.reader().read(gzis)
                parseNBTStructure(tag)
            }
        } catch (e2: Exception) {
            throw IllegalStateException("Failed to read NBT file: ${file.path}", e2)
        }
    }
}

private fun parseNBTStructure(tag: Any): NBTStructure {
    val compoundTag = tag as? CompoundBinaryTag 
        ?: throw IllegalArgumentException("Expected CompoundBinaryTag but got ${tag::class.simpleName}")
    
    val sizeTag = compoundTag.getList("size")
    val size = (0 until sizeTag.size()).map { sizeTag.getInt(it) }

    val paletteTag = compoundTag.getList("palette")
    val palette = (0 until paletteTag.size()).map { i ->
        val blockTag = paletteTag.getCompound(i)
        val name = blockTag.getString("Name")
        val properties = if (blockTag.keySet().contains("Properties")) {
            val propsTag = blockTag.getCompound("Properties")
            propsTag.keySet().associateWith { key ->
                propsTag.getString(key)
            }
        } else {
            null
        }
        PaletteBlock(name = BlockID.fromString(name), properties = properties)
    }

    val blocksTag = tag.getList("blocks")
    val blocks = (0 until blocksTag.size()).map { i ->
        val blockTag = blocksTag.getCompound(i)
        val state = blockTag.getInt("state")
        val posTag = blockTag.getList("pos")
        val pos = listOf(posTag.getInt(0), posTag.getInt(1), posTag.getInt(2))
        val nbt = if (blockTag.keySet().contains("nbt")) {
            blockTag.getString("nbt")
        } else {
            null
        }
        BlockData(state = state, pos = pos, nbt = nbt)
    }

    val entities = if (compoundTag.keySet().contains("entities")) {
        val entitiesTag = compoundTag.getList("entities")
        (0 until entitiesTag.size()).map { i ->
            val entityTag = entitiesTag.getCompound(i)
            val posTag = entityTag.getList("pos")
            val pos = listOf(posTag.getDouble(0), posTag.getDouble(1), posTag.getDouble(2))
            val blockPosTag = entityTag.getList("blockPos")
            val blockPos = listOf(blockPosTag.getInt(0), blockPosTag.getInt(1), blockPosTag.getInt(2))
            val nbt = entityTag.getString("nbt")
            Entity(pos = pos, blockPos = blockPos, nbt = nbt)
        }
    } else {
        emptyList()
    }

    return NBTStructure(size = size, palette = palette, blocks = blocks, entities = entities)
}
