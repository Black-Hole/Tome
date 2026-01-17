package generator.buildings.roofs

import data.Loadable
import data.Point3DSerializer
import editor.Editor
import generator.buildings.BuildingData
import generator.data.LoadedData
import generator.materials.Placer
import generator.nbts.Structure
import generator.nbts.StructureId
import generator.nbts.placeStructure
import generator.style.Style
import geometry.Cardinal
import geometry.Point3D
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import noise.RNG

@Serializable
data class RoofSetId(val value: String) {
    override fun toString(): String = value
}

@Serializable
data class RoofSet(
    val id: RoofSetId,
    val style: Style,
    val side: StructureId,
    val corner: StructureId,
    val inner: StructureId
) {
    companion object : Loadable<RoofSet, RoofSetId> {
        override fun getKey(item: RoofSet): RoofSetId = item.id

        override fun postLoad(items: MutableMap<RoofSetId, RoofSet>) {
            // No post-processing needed
        }

        override fun path(): String = "buildings/roofs/sets"

        override fun deserializer() = serializer()
    }
}

@Serializable
data class RoofComponent(
    @SerialName("id")
    val structureId: StructureId,
    @SerialName("path")
    val structurePath: String,
    val facing: Cardinal = Cardinal.NORTH,
    @Serializable(with = Point3DSerializer::class)
    val origin: Point3D = Point3D(0, 0, 0),
    val palette: generator.materials.PaletteId? = null,
    val tags: List<String>? = null,
    @SerialName("mirror_x")
    val mirrorX: Boolean = false,
    @SerialName("mirror_z")
    val mirrorZ: Boolean = false,
    val style: Style? = null,
    val weight: Float = 1.0f,
    @SerialName("roof_type")
    val roofType: RoofType
) {
    val structure: Structure by lazy {
        Structure(
            id = structureId,
            meta = generator.nbts.NBTMeta(structurePath),
            facing = facing,
            origin = origin,
            palette = palette,
            tags = tags,
            mirrorX = mirrorX,
            mirrorZ = mirrorZ,
            style = style,
            weight = weight
        )
    }

    companion object : Loadable<RoofComponent, StructureId> {
        override fun getKey(item: RoofComponent): StructureId = item.structureId

        override fun postLoad(items: MutableMap<StructureId, RoofComponent>) {
            // No post-processing needed
        }

        override fun path(): String = "buildings/roofs/components"

        override fun deserializer() = serializer()
    }
}

@Serializable
sealed class RoofType {
    @Serializable
    @SerialName("gable")
    data object Gable : RoofType()

    @Serializable
    @SerialName("hip")
    data class Hip(val shape: HipRoofPart) : RoofType()
}

@Serializable
enum class HipRoofPart {
    @SerialName("side")
    SIDE,

    @SerialName("corner")
    CORNER,

    @SerialName("inner")
    INNER
}

suspend fun buildRoof(
    editor: Editor,
    data: LoadedData,
    building: BuildingData,
    rng: RNG
): Result<Unit> {
    return try {
        val placerRng = rng.derive()
        val placer = Placer(data.materials, placerRng)

        val sets = data.roofSets.values.filter { it.style == building.style }
        val roofSet = rng.choose(sets)

        val side = data.roofComponents[roofSet.side] 
            ?: error("Roof set should have a side component")
        val corner = data.roofComponents[roofSet.corner]
            ?: error("Roof set should have a corner component")
        val inner = data.roofComponents[roofSet.inner]
            ?: error("Roof set should have an inner component")

        for (cell in building.shape.cells()) {
            if (building.shape.cells().any { otherCell -> otherCell == cell + Point3D.UP }) {
                continue // skip cells that have a roof above them
            }

            val neighbours: Map<Cardinal, Boolean> = Cardinal.entries.associateWith { direction ->
                val neighbourCell = cell + direction.toPoint3D()
                building.shape.cells().any { otherCell -> otherCell == neighbourCell }
            }

            for (direction in Cardinal.entries) {
                var offset = building.grid.getDoorWorldPosition(cell + Point3D.UP, direction.rotateLeft())

                if (!neighbours[direction]!! && !neighbours[direction.rotateLeft()]!!) {
                    offset += direction.toPoint3D() * when (direction) {
                        Cardinal.NORTH, Cardinal.SOUTH -> building.grid.cellSize.z / 2
                        Cardinal.EAST, Cardinal.WEST -> building.grid.cellSize.x / 2
                    }
                    generator.nbts.placeStructure(
                        editor = editor,
                        placer = placer,
                        structure = corner.structure,
                        offset = offset,
                        direction = direction,
                        materials = data.materials,
                        palettes = data.palettes,
                        palette = building.palette,
                        mirrorX = false,
                        mirrorZ = false
                    )
                } else if (!neighbours[direction]!!) {
                    offset += direction.rotateRight().toPoint3D() * when (direction) {
                        Cardinal.NORTH, Cardinal.SOUTH -> building.grid.cellSize.z / 2
                        Cardinal.EAST, Cardinal.WEST -> building.grid.cellSize.x / 2
                    } + direction.toPoint3D() * when (direction) {
                        Cardinal.NORTH, Cardinal.SOUTH -> building.grid.cellSize.z / 2
                        Cardinal.EAST, Cardinal.WEST -> building.grid.cellSize.x / 2
                    }

                    generator.nbts.placeStructure(
                        editor = editor,
                        placer = placer,
                        structure = side.structure,
                        offset = offset,
                        direction = direction.rotateRight(),
                        materials = data.materials,
                        palettes = data.palettes,
                        palette = building.palette,
                        mirrorX = false,
                        mirrorZ = false
                    )
                } else if (!neighbours[direction.rotateLeft()]!!) {
                    generator.nbts.placeStructure(
                        editor = editor,
                        placer = placer,
                        structure = side.structure,
                        offset = offset,
                        direction = direction,
                        materials = data.materials,
                        palettes = data.palettes,
                        palette = building.palette,
                        mirrorX = false,
                        mirrorZ = true
                    )
                } else {
                    offset += direction.toPoint3D() * when (direction) {
                        Cardinal.NORTH, Cardinal.SOUTH -> building.grid.cellSize.z / 2
                        Cardinal.EAST, Cardinal.WEST -> building.grid.cellSize.x / 2
                    }
                    generator.nbts.placeStructure(
                        editor = editor,
                        placer = placer,
                        structure = inner.structure,
                        offset = offset,
                        direction = direction,
                        materials = data.materials,
                        palettes = data.palettes,
                        palette = building.palette,
                        mirrorX = false,
                        mirrorZ = false
                    )
                }
            }
        }

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
