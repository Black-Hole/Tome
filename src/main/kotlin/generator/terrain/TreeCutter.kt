package generator.terrain

import editor.Editor
import geometry.Point2D
import geometry.Point3D
import minecraft.Block
import minecraft.BlockID
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("TreeCutter")

suspend fun logStems(editor: Editor, points: Set<Point2D>) {
    for (point in points) {
        val height = editor.worldMut().getHeightAt(point) - 1
        var blockId = editor.getBlock(Point3D(point.x, height, point.y)).id

        if (!blockId.isTree()) {
            continue
        }
        editor.placeBlock(Block(BlockID.AIR, null, null), Point3D(point.x, height, point.y))

        for (y in 1 until 40) {
            blockId = editor.getBlock(Point3D(point.x, height - y, point.y)).id
            if (blockId.isTree()) {
                editor.placeBlock(Block(BlockID.AIR, null, null), Point3D(point.x, height - y, point.y))
            } else if (blockId == BlockID.DIRT) {
                editor.placeBlock(Block(BlockID.GRASS_BLOCK, null, null), Point3D(point.x, height - y, point.y))
            } else if (blockId != BlockID.AIR) {
                continue
            }
        }
    }
}

suspend fun logTrees(editor: Editor, points: Set<Point2D>) {
    for (point in points) {
        val height = editor.worldMut().getMotionBlockingHeightAt(point) - 1
        val point3d = Point3D(point.x, height, point.y)
        var blockId = editor.getBlock(point3d).id

        if (!blockId.isTreeOrLeaf()) {
            continue
        }
        editor.placeBlock(Block(BlockID.AIR, null, null), point3d)
        
        for (y in 1 until 40) {
            blockId = editor.getBlock(Point3D(point.x, height - y, point.y)).id
            if (blockId.isTreeOrLeaf()) {
                editor.placeBlock(Block(BlockID.AIR, null, null), Point3D(point.x, height - y, point.y))
            } else if (blockId == BlockID.DIRT) {
                editor.placeBlock(Block(BlockID.GRASS_BLOCK, null, null), Point3D(point.x, height - y, point.y))
            } else if (blockId != BlockID.AIR) {
                continue
            }
        }
    }
}
