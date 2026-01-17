package generator.buildings.walls

import data.Loadable
import data.Point3DSerializer
import editor.Editor
import generator.buildings.BuildingData
import generator.data.LoadedData
import generator.materials.Placer
import generator.nbts.Structure
import generator.nbts.StructureId
import geometry.Cardinal
import geometry.Point3D
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import noise.RNG

@Serializable
data class Wall(
    @SerialName("id")
    val structureId: StructureId,
    @SerialName("path")
    val structurePath: String,
    val facing: Cardinal? = null,
    @Serializable(with = Point3DSerializer::class)
    val origin: geometry.Point3D? = null,
    val palette: generator.materials.PaletteId? = null,
    val tags: List<String>? = null,
    @SerialName("mirror_x")
    val mirrorX: Boolean = false,
    @SerialName("mirror_z")
    val mirrorZ: Boolean = false,
    val style: generator.style.Style? = null,
    val weight: Float = 1.0f,
    @SerialName("wall_type")
    val wallType: WallType? = null,
    @SerialName("vertical_position")
    val verticalPosition: VerticalWallPosition? = null,
    @SerialName("horizontal_position")
    val horizontalPosition: HorizontalWallPosition? = null
) {
    // Create the internal Structure object from the flat fields
    val structure: Structure by lazy {
        Structure(
            id = structureId,
            meta = generator.nbts.NBTMeta(structurePath),
            facing = facing ?: Cardinal.NORTH,
            origin = origin ?: geometry.Point3D(0, 0, 0),
            palette = palette,
            tags = tags,
            mirrorX = mirrorX,
            mirrorZ = mirrorZ,
            style = style,
            weight = weight
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Wall) return false
        return structure == other.structure
    }

    override fun hashCode(): Int {
        return structure.hashCode()
    }

    companion object : Loadable<Wall, StructureId> {
        override fun getKey(item: Wall): StructureId = item.structureId

        override fun postLoad(items: MutableMap<StructureId, Wall>) {
            // No post-processing needed
        }

        override fun path(): String = "buildings/walls"

        override fun deserializer() = serializer()
    }
}

@Serializable
enum class WallType {
    @SerialName("window")
    WINDOW,
    
    @SerialName("door")
    DOOR
}

@Serializable
enum class VerticalWallPosition {
    @SerialName("top")
    TOP, // Can only be on the top layer of a building
    
    @SerialName("non_bottom")
    NON_BOTTOM, // Can be on any layer except the bottom layer of a building
    
    @SerialName("bottom")
    BOTTOM, // Can only be on the bottom layer of a building
    
    @SerialName("middle")
    MIDDLE, // Cannot be on the top or bottom layer of a building
    
    @SerialName("single")
    SINGLE // Has nothing above or below it
}

@Serializable
enum class HorizontalWallPosition {
    @SerialName("end")
    END // Has no neighbours
}

suspend fun buildWalls(
    editor: Editor,
    walls: List<Wall>,
    building: BuildingData,
    data: LoadedData,
    rng: RNG
): Result<Unit> {
    return try {
        for (cell in building.shape.cells()) {
            val isBottom = !building.shape.cells().contains(cell + Point3D.DOWN)
            val isTop = !building.shape.cells().contains(cell + Point3D.UP)

            for (direction in Cardinal.entries) {
                if (building.shape.cells().any { otherCell -> otherCell == cell + direction.toPoint3D() }) {
                    continue
                }

                val filteredWalls = walls.filter { wall ->
                    (wall.structure.style == null || wall.structure.style == building.style) &&
                    (wall.verticalPosition == null || when (wall.verticalPosition) {
                        VerticalWallPosition.TOP -> isTop
                        VerticalWallPosition.NON_BOTTOM -> !isBottom
                        VerticalWallPosition.BOTTOM -> isBottom
                        VerticalWallPosition.MIDDLE -> !isTop && !isBottom
                        VerticalWallPosition.SINGLE -> isBottom && isTop
                    })
                }.associateWith { it.structure.weight }

                val wall = rng.chooseWeighted(filteredWalls)

                val placer = Placer(data.materials, rng)
                building.grid.buildStructure(
                    editor = editor,
                    placer = placer,
                    structure = wall.structure,
                    gridCoordinate = cell,
                    direction = direction,
                    data = data,
                    palette = building.palette
                )
            }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
