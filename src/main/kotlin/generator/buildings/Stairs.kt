package generator.buildings

import editor.Editor
import generator.data.LoadedData
import generator.materials.MaterialPlacer
import generator.materials.MaterialRole
import generator.materials.Placer
import geometry.*
import minecraft.BlockForm
import minecraft.BlockID
import noise.RNG

suspend fun buildStairs(editor: Editor, building: BuildingData, data: LoadedData, rng: RNG) {
    val stairs = building.shape.stairs() ?: error("Building shape must have stairs defined")

    for (stair in stairs) {
        buildStair(
            editor = editor,
            building = building,
            data = data,
            cell = stair.cell,
            direction = stair.direction,
            rng = rng,
            leftToRight = stair.leftToRight
        )
    }
}

suspend fun buildStair(
    editor: Editor,
    building: BuildingData,
    data: LoadedData,
    cell: Point3D,
    direction: Cardinal,
    rng: RNG,
    leftToRight: Boolean
) {
    val palette = data.palettes[building.palette]
    val woodId = palette?.getMaterial(MaterialRole.SECONDARY_WOOD)
        ?: error("Wood block not found in palette")

    val placer = MaterialPlacer(
        placer = Placer(data.materials, rng),
        material = woodId
    )

    val refPoint = building.grid.getDoorWorldPosition(cell, direction)

    val innerVec: Point3D = (-direction).toPoint3D()

    val leftVec = if (leftToRight) {
        innerVec.rotateLeft()
    } else {
        innerVec.rotateRight()
    }

    val facing: String = if (leftToRight) {
        direction.rotateRight().toString()
    } else {
        direction.rotateLeft().toString()
    }

    val facingAway = if (leftToRight) {
        direction.rotateLeft().toString()
    } else {
        direction.rotateRight().toString()
    }

    // First air block
    editor.placeBlockForced(
        minecraft.Block(BlockID.AIR, null, null),
        refPoint + innerVec + leftVec * 2 + Point3D.UP * (building.grid.cellSize.y - 2)
    )

    for (i in 0 until building.grid.cellSize.y - 1) {
        // Clear air
        editor.placeBlockForced(
            minecraft.Block(BlockID.AIR, null, null),
            refPoint + innerVec + leftVec * (1 - i) + Point3D.UP * (building.grid.cellSize.y - 2)
        )

        // Stairs
        placer.placeBlockForced(
            editor = editor,
            point = refPoint + innerVec + leftVec * (1 - i) + Point3D.UP * i,
            form = BlockForm.STAIRS,
            state = mapOf("facing" to facing),
            data = null
        )

        // Underside - not forced so we don't overwrite the bottom floor
        placer.placeBlock(
            editor = editor,
            point = refPoint + innerVec + leftVec * (1 - i) + Point3D.UP * (i - 1),
            form = BlockForm.STAIRS,
            state = mapOf(
                "facing" to facingAway,
                "half" to "top"
            ),
            data = null
        )
    }
}
