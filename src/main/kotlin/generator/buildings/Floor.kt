package generator.buildings

import editor.Editor
import generator.data.LoadedData
import generator.materials.MaterialPlacer
import generator.materials.MaterialRole
import generator.materials.Placer
import geometry.*
import minecraft.BlockForm
import noise.RNG

suspend fun buildFloor(editor: Editor, data: LoadedData, building: BuildingData, rng: RNG) {
    val palette = data.palettes[building.palette] ?: error("Palette not found")
    val woodId = palette.getMaterial(MaterialRole.SECONDARY_WOOD) 
        ?: error("Secondary wood material not found")
    
    val placer = MaterialPlacer(
        placer = Placer(data.materials, rng),
        material = woodId
    )

    for (cell in building.shape.cells()) {
        var min = Point2D(1, 1)
        var max = building.grid.cellSize.dropY() - Point2D(2, 2)

        for (direction in Cardinal.entries) {
            if (building.shape.cells().any { otherCell -> otherCell == cell + direction.toPoint3D() }) {
                when (direction) {
                    Cardinal.NORTH -> min = min.copy(y = min.y - 1)
                    Cardinal.EAST -> max = max.copy(x = max.x + 1)
                    Cardinal.SOUTH -> max = max.copy(y = max.y + 1)
                    Cardinal.WEST -> min = min.copy(x = min.x - 1)
                }
            }
        }

        for (x in min.x..max.x) {
            for (z in min.y..max.y) {
                val point = building.grid.gridToWorld(cell) + Point3D(x, 0, 0) + Point3D(0, 0, z) + Point3D.DOWN
                placer.placeBlock(editor, point, BlockForm.BLOCK, null, null)
            }
        }
    }
}
