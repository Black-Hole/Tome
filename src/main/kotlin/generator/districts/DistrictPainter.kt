package generator.districts

import editor.Editor
import generator.BuildClaim
import generator.terrain.Forest
import generator.terrain.generateTree
import geometry.Point2D
import geometry.Point3D
import geometry.cardinalToStr
import minecraft.Block
import minecraft.BlockID
import noise.RNG

suspend fun replaceGround(
    points: Set<Point2D>,
    blockDict: Map<Int, Float>,
    blockList: List<Block>,
    rng: RNG,
    editor: Editor,
    heightOffset: Int? = null,
    permitBlocks: Set<BlockID>? = null,
    ignoreWater: Boolean? = null
) {
    for (point in points) {
        if (editor.world().isClaimed(point)) {
            continue
        }
        if (ignoreWater == true && editor.world().isWater(point)) {
            continue
        }

        var height = editor.world().getHeightAt(point) - 1
        val block = editor.getBlock(Point3D(point.x, height, point.y))

        if (permitBlocks != null && permitBlocks.contains(block.id)) {
            continue
        }

        if (heightOffset != null) {
            height += heightOffset
        }

        val blockPos = rng.chooseWeighted(blockDict)
        editor.placeBlock(blockList[blockPos], Point3D(point.x, height, point.y))
    }
}

suspend fun replaceGroundSmooth(
    points: Set<Point2D>,
    blockDict: Map<Int, Map<Int, Float>>,
    blockList: List<Block>,
    rng: RNG,
    editor: Editor,
    heightOffset: Int? = null,
    permitBlocks: Set<BlockID>? = null,
    ignoreWater: Boolean? = null
) {
    for (point in points) {
        if (editor.world().isClaimed(point)) {
            continue
        }
        if (ignoreWater == true && editor.world().isWater(point)) {
            continue
        }

        var height = editor.world().getHeightAt(point)
        val block = editor.getBlock(Point3D(point.x, height, point.y))

        if (permitBlocks != null && permitBlocks.contains(block.id)) {
            continue
        }

        if (heightOffset != null) {
            height += heightOffset
        }

        val yInDir = mutableMapOf<Point2D, Int>()
        var selectedBlock = Block(BlockID.UNKNOWN, null, null)

        val cardinals2d = Point2D.CARDINALS_2D
        for (direction in cardinals2d) {
            val neighbor = point + direction
            val oppositeNeighbour = point - direction

            if (!points.contains(neighbor)) {
                continue
            }

            if (points.contains(neighbor)) {
                yInDir[direction] = editor.world().getHeightAt(neighbor)
            }

            if (!points.contains(oppositeNeighbour)) {
                continue
            }

            if (editor.world().getHeightAt(neighbor) == height + 1 &&
                editor.world().getHeightAt(oppositeNeighbour) == height - 1
            ) {
                selectedBlock = blockList[rng.chooseWeighted(blockDict[1]!!)].copy()
                selectedBlock = selectedBlock.copy(state = mapOf("facing" to (cardinalToStr(direction) ?: "")))
                break
            }
        }

        if (yInDir.values.all { it <= height } && yInDir.values.any { it < height }) {
            selectedBlock = blockList[rng.chooseWeighted(blockDict[2]!!)].copy()
        }

        if (selectedBlock.id == BlockID.UNKNOWN) {
            selectedBlock = blockList[rng.chooseWeighted(blockDict[0]!!)].copy()
        }

        editor.placeBlock(selectedBlock, Point3D(point.x, height - 1, point.y))
    }
}

suspend fun plantForest(
    points: Set<Point2D>,
    forest: Forest,
    rng: RNG,
    editor: Editor,
    permitBlocks: Set<BlockID>? = null,
    ignoreWater: Boolean = false
) {
    val shuffledPoints = points.toMutableList()

    for (point in points) {
        val randomPoint = rng.pop(shuffledPoints) ?: point

        if (editor.world().isClaimed(randomPoint)) {
            continue
        }
        if (ignoreWater && editor.world().isWater(randomPoint)) {
            continue
        }

        val height = editor.world().getHeightAt(randomPoint)
        val block = editor.getBlock(Point3D(randomPoint.x, height, randomPoint.y))

        if (permitBlocks != null && permitBlocks.contains(block.id)) {
            continue
        }

        val treeType = rng.chooseWeighted(forest.trees())
        val palette = forest.treePalette()[treeType]
            ?: error("Tree type not found in forest palette")

        generateTree(treeType, editor, randomPoint.addY(height), rng, palette)

        for (x in (randomPoint.x - forest.treeDensity() + 1) until (randomPoint.x + forest.treeDensity())) {
            for (y in (randomPoint.y - forest.treeDensity() + 1) until (randomPoint.y + forest.treeDensity())) {
                editor.world().claim(Point2D(x, y), BuildClaim.Nature)
            }
        }
    }
}
