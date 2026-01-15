package generator.paths

import editor.Editor
import generator.materials.MaterialPlacer
import generator.materials.Placer
import generator.materials.MaterialLoader
import geometry.Point2D
import geometry.Point3D
import geometry.getSurroundingSet
import minecraft.Block
import minecraft.BlockForm
import minecraft.BlockID
import noise.RNG
import util.mean

suspend fun buildPath(
    editor: Editor,
    path: Path,
    rng: RNG
) {
    val points2d = path.points()
        .map { it.dropY() }
        .toMutableSet()
    
    val surroundingPoints = getSurroundingSet(points2d, path.width() - 1)
        .filter { editor.world().isInBounds2d(it) }
    for (point in surroundingPoints) {
        points2d.add(point)
    }
    
    val heightByPoint = path.points()
        .associate { p ->
            p.dropY() to p.y.toFloat()
        }
        .toMutableMap()
    
    for (point in points2d) {
        val neighbourHeights = (point.neighbours() + point)
            .filter { it in heightByPoint }
            .map { heightByPoint[it]!! }
        heightByPoint[point] = neighbourHeights.mean()
    }
    
    for (point in points2d) {
        val neighbourHeights = (point.neighbours() + point)
            .filter { it in heightByPoint }
            .map { heightByPoint[it]!! }
        heightByPoint[point] = neighbourHeights.mean()
    }
    
    for (point in points2d) {
        val allHigher = point.neighbours().all { neighbour ->
            neighbour !in heightByPoint || heightByPoint[neighbour]!! > heightByPoint[point]!!
        }
        if (allHigher) {
            heightByPoint[point] = heightByPoint[point]!! + 1.0f
            continue
        }
        
        val allLower = point.neighbours().all { neighbour ->
            neighbour !in heightByPoint || heightByPoint[neighbour]!! < heightByPoint[point]!!
        }
        if (allLower) {
            heightByPoint[point] = heightByPoint[point]!! - 1.0f
            continue
        }
    }
    
    for (point in points2d) {
        val neighbourHeights = (point.neighbours() + point)
            .filter { it in heightByPoint }
            .map { heightByPoint[it]!! }
        heightByPoint[point] = neighbourHeights.mean()
    }
    
    val materials = MaterialLoader.load()
    val placer = MaterialPlacer(
        Placer(materials, rng),
        path.material()
    )
    
    for (point in points2d) {
        val height = heightByPoint[point] ?: error("Height for point should be calculated")
        val intHeight = kotlin.math.floor(height).toInt()
        val point3d = Point3D(
            x = point.x,
            y = intHeight,
            z = point.y
        )
        
        val remainder = height - intHeight.toFloat()
        
        for (i in 0..3) {
            editor.placeBlock(Block.from(BlockID.AIR), point3d + Point3D.UP * i)
        }
        
        if (remainder > 0.3f) {
            placer.placeBlock(editor, point3d, BlockForm.SLAB, null, null)
        }
        
        placer.placeBlock(editor, point3d + Point3D.DOWN, BlockForm.BLOCK, null, null)
    }
}
