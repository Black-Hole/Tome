package generator.buildings

import editor.Editor
import generator.data.LoadedData
import generator.materials.PaletteId
import generator.materials.Placer
import generator.nbts.*
import geometry.*

data class Grid(
    val origin: Point3D,
    val cellSize: Point3D = DEFAULT_GRID_CELL_SIZE
) {
    companion object {
        val DEFAULT_GRID_CELL_SIZE = Point3D(7, 5, 7)
        
        fun new(origin: Point3D): Grid = Grid(origin)
    }

    fun gridToWorld(point: Point3D): Point3D {
        return Point3D(
            x = point.x * (cellSize.x - 1) + origin.x,
            y = point.y * (cellSize.y - 1) + origin.y,
            z = point.z * (cellSize.z - 1) + origin.z
        )
    }

    fun worldToGrid(point: Point3D): Point3D {
        return Point3D(
            x = (point.x - origin.x) / (cellSize.x - 1),
            y = (point.y - origin.y) / (cellSize.y - 1),
            z = (point.z - origin.z) / (cellSize.z - 1)
        )
    }

    fun gridToLocal(point: Point3D): Point3D {
        return Point3D(
            x = point.x * (cellSize.x - 1),
            y = point.y * (cellSize.y - 1),
            z = point.z * (cellSize.z - 1)
        )
    }

    fun localToGrid(point: Point3D): Point3D {
        return Point3D(
            x = point.x / (cellSize.x - 1),
            y = point.y / (cellSize.y - 1),
            z = point.z / (cellSize.z - 1)
        )
    }

    fun localToWorld(point: Point3D): Point3D {
        return point + origin
    }

    fun worldToLocal(point: Point3D): Point3D {
        return point - origin
    }

    suspend fun buildStructure(
        editor: Editor,
        placer: Placer,
        structure: Structure,
        gridCoordinate: Point3D,
        direction: Cardinal,
        data: LoadedData,
        palette: PaletteId
    ) {
        val buildOrigin = gridToWorld(gridCoordinate)

        val rotation: Rotation = Rotation.fromCardinal(structure.facing) - Rotation.fromCardinal(direction)

        println("Facing: ${structure.facing}, Direction: $direction, Rotation: $rotation")
        println("Building structure: ${structure.id} at grid coordinate: $gridCoordinate with rotation: $rotation")

        val transform = when (rotation) {
            Rotation.NONE -> Transform(buildOrigin, Rotation.NONE)
            Rotation.ONCE -> Transform(buildOrigin + Point3D(0, 0, cellSize.z - 1), Rotation.ONCE)
            Rotation.TWICE -> Transform(buildOrigin + Point3D(cellSize.x - 1, 0, cellSize.z - 1), Rotation.TWICE)
            Rotation.THRICE -> Transform(buildOrigin + Point3D(cellSize.x - 1, 0, 0), Rotation.THRICE)
        }

        // Shift the transform to account for the structure's origin
        transform.shift(rotation.applyToPoint(-structure.origin))

        val inputPalette = structure.palette

        generator.nbts.placeNBT(
            meta = structure.meta,
            transform = transform,
            editor = editor,
            placer = placer,
            materials = data.materials,
            palettes = data.palettes,
            inputPalette = inputPalette,
            outputPalette = palette,
            mirrorX = null,
            mirrorZ = null
        )
    }

    suspend fun buildNBT(
        editor: Editor,
        placer: Placer,
        nbt: NBTMeta,
        gridCoordinate: Point3D,
        rotation: Rotation,
        data: LoadedData,
        inputPalette: PaletteId,
        outputPalette: PaletteId
    ) {
        val buildOrigin = gridToWorld(gridCoordinate)

        val transform = when (rotation) {
            Rotation.NONE -> Transform(buildOrigin, Rotation.NONE)
            Rotation.ONCE -> Transform(buildOrigin + Point3D(0, 0, cellSize.z - 1), Rotation.ONCE)
            Rotation.TWICE -> Transform(buildOrigin + Point3D(cellSize.x - 1, 0, cellSize.z - 1), Rotation.TWICE)
            Rotation.THRICE -> Transform(buildOrigin + Point3D(cellSize.x - 1, 0, 0), Rotation.THRICE)
        }

        generator.nbts.placeNBT(
            meta = nbt,
            transform = transform,
            editor = editor,
            placer = placer,
            materials = data.materials,
            palettes = data.palettes,
            inputPalette = inputPalette,
            outputPalette = outputPalette,
            mirrorX = null,
            mirrorZ = null
        )
    }

    fun getDoorWorldPosition(gridCoordinate: Point3D, direction: Cardinal): Point3D {
        val offset = gridToWorld(gridCoordinate)
        return when (direction) {
            Cardinal.NORTH -> offset + Point3D(cellSize.x / 2, 0, 0)
            Cardinal.WEST -> offset + Point3D(0, 0, cellSize.z / 2)
            Cardinal.SOUTH -> offset + Point3D(cellSize.x / 2, 0, cellSize.z - 1)
            Cardinal.EAST -> offset + Point3D(cellSize.x - 1, 0, cellSize.z / 2)
        }
    }

    fun getCellRect(gridCoordinate: Point3D): Rect3D {
        val local = gridToLocal(gridCoordinate)
        return Rect3D(
            origin = local,
            size = cellSize
        )
    }

    fun getCellRect2D(gridCoordinate: Point3D): Rect2D {
        return getCellRect(gridCoordinate).dropY()
    }
}
