package generator.districts

import editor.Editor
import editor.World
import generator.BuildClaim
import generator.materials.MaterialId
import generator.materials.Placer
import generator.nbts.Structure
import generator.nbts.StructureId
import generator.nbts.placeStructure
import geometry.*
import minecraft.Block
import minecraft.BlockForm
import minecraft.BlockID
import noise.RNG
import org.slf4j.LoggerFactory
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

private val logger = LoggerFactory.getLogger("Wall")

const val WALL_HEIGHT = 10
const val WATER_CHECK = 5
const val RANGE = 3

enum class WallType {
    Water,
    WaterWall,
    Standard,
    Palisade,
    StandardWithInner
}

fun getWallPoints(
    innerPoints: Set<Point2D>,
    editor: Editor
): Set<Point2D> {
    val wallPoints = getOuterPoints(innerPoints).toMutableSet()
    val toRemove = mutableListOf<Point2D>()

    for (point in wallPoints) {
        editor.world().claim(point, BuildClaim.Wall)
    }

    for (point in toRemove) {
        wallPoints.remove(point)
    }

    return wallPoints
}

fun findWallNeighbour(
    point: Point2D,
    wallPoints: Set<Point2D>,
    orderedSet: Set<Point2D>
): Point2D? {
    val directions = listOf(
        Point2D(-1, 0),
        Point2D(0, -1),
        Point2D(-1, -1),
        Point2D(-1, 1),
        Point2D(1, -1),
        Point2D(1, 0),
        Point2D(0, 1),
        Point2D(1, 1)
    )

    for (direction in directions) {
        val neighbour = point + direction
        if (!orderedSet.contains(neighbour) && wallPoints.contains(neighbour)) {
            return neighbour
        }
    }
    return null
}

fun orderWallPoints(wallPoints: Set<Point2D>): List<List<Point2D>> {
    val listOfOrderedVec = mutableListOf<List<Point2D>>()
    val wallPointList = wallPoints.toMutableList()
    var orderedVec = mutableListOf<Point2D>()
    val orderedSet = mutableSetOf<Point2D>()
    var currentPoint = wallPointList.removeAt(0)

    orderedVec.add(currentPoint)
    orderedSet.add(currentPoint)

    var reverseCheck = false

    while (wallPointList.isNotEmpty()) {
        val nextWallPoint = findWallNeighbour(currentPoint, wallPoints, orderedSet)

        if (nextWallPoint == null) {
            if (reverseCheck) {
                logger.info("Failed to find a neighbour")
                reverseCheck = false
                if (orderedVec.size > 20) {
                    listOfOrderedVec.add(orderedVec.toList())
                }
                orderedVec.clear()
                currentPoint = wallPointList.removeAt(0)
                orderedVec.add(currentPoint)
                orderedSet.add(currentPoint)
                break
            } else {
                logger.info("Reversing wall")
                reverseCheck = true
                orderedVec.reverse()
                currentPoint = orderedVec.first()
                continue
            }
        } else {
            wallPointList.remove(nextWallPoint)
            orderedVec.add(currentPoint)
            orderedSet.add(currentPoint)
            currentPoint = nextWallPoint
        }
    }

    listOfOrderedVec.add(orderedVec)
    return listOfOrderedVec
}

suspend fun buildWall(
    urbanPoints: Set<Point2D>,
    editor: Editor,
    rng: RNG,
    materialPlacer: Placer,
    materialId: MaterialId,
    structures: Map<StructureId, Structure>,
    wallType: WallType
) {
    val wallPoints = getWallPoints(urbanPoints, editor)
    val orderedWallPoints = orderWallPoints(wallPoints)

    for (wallPointList in orderedWallPoints) {
        when (wallType) {
            WallType.Standard -> buildWallStandard(
                wallPointList,
                editor,
                rng,
                materialPlacer,
                materialId,
                structures,
                urbanPoints
            )
            WallType.Palisade -> buildWallPalisade(
                wallPointList,
                editor,
                rng,
                materialPlacer,
                materialId,
                structures
            )
            WallType.StandardWithInner -> buildWallStandardWithInner(
                wallPointList,
                editor,
                rng,
                materialPlacer,
                materialId,
                structures,
                urbanPoints
            )
            else -> {}
        }
    }
}

suspend fun buildWallPalisade(
    wallPoints: List<Point2D>,
    editor: Editor,
    rng: RNG,
    materialPlacer: Placer,
    materialId: MaterialId,
    structures: Map<StructureId, Structure>
) {
    val wallPointsWithHeight = wallPoints.associate { point ->
        val height = rng.randI32Range(4, 7)
        val newPoint = editor.world().addHeight(point)
        newPoint to height
    }

    val mainPoints = mutableListOf<Point3D>()
    val topPoints = mutableListOf<Point3D>()
    val wallPointsWithWorldHeight = wallPoints.map { editor.world().addHeight(it) }

    for ((point, height) in wallPointsWithHeight) {
        if (editor.world().isWater(point.dropY())) {
            continue
        }
        for (y in point.y until point.y + height) {
            mainPoints.add(Point3D(point.x, y, point.z))
        }
        topPoints.add(Point3D(point.x, point.y + height, point.z))
    }

    materialPlacer.placeBlocks(
        editor,
        mainPoints.toList(),
        materialId,
        BlockForm.LOG,
        null,
        null
    )
    materialPlacer.placeBlocks(
        editor,
        topPoints.toList(),
        materialId,
        BlockForm.FENCE,
        null,
        null
    )

    buildWallGate(
        wallPointsWithWorldHeight,
        editor,
        rng,
        materialPlacer,
        true,
        true,
        null,
        null,
        structures,
        10
    )
}

suspend fun buildWallStandard(
    wallPoints: List<Point2D>,
    editor: Editor,
    rng: RNG,
    materialPlacer: Placer,
    materialId: MaterialId,
    structures: Map<StructureId, Structure>,
    urbanPoints: Set<Point2D>
) {
    val wallPointsWithHeight = addWallPointsHeight(wallPoints, editor)
    val enhancedWallPoints = checkWater(
        addWallPointsDirectionality(
            wallPointsWithHeight,
            wallPoints.toSet(),
            urbanPoints
        ).toMutableList(),
        editor
    )

    val walkwayPoints = mutableListOf<Point2D>()
    val walkwayHeights = mutableMapOf<Point2D, Int>()

    var previousDir = Cardinal.NORTH

    for ((i, triple) in enhancedWallPoints.withIndex()) {
        val (point, directions, wallType) = triple
        if (wallType == WallType.Water) {
            continue
        } else {
            if (wallType == WallType.WaterWall) {
                fillWater(point.dropY(), editor, materialPlacer, materialId)
            }

            for (y in editor.world().getHeightAt(point.dropY())..point.y) {
                val newPoint = Point3D(point.x, y, point.z)
                materialPlacer.placeBlock(editor, newPoint, materialId, BlockForm.BLOCK, null, null)
            }

            if (directions.isNotEmpty()) {
                previousDir = directions[0]
            }
            val state = mapOf("facing" to previousDir.rotateRight().toString())
            materialPlacer.placeBlock(
                editor,
                Point3D(point.x, point.y + 1, point.z),
                materialId,
                BlockForm.STAIRS,
                state,
                null
            )

            for (dir in directions) {
                var heightModifier = 0

                if (i != 0 && i != enhancedWallPoints.size - 1) {
                    val prevH = enhancedWallPoints[i - 1].first.y
                    val nextH = enhancedWallPoints[i + 1].first.y
                    val h = point.y
                    if (prevH == h - 1 && nextH == h - 1) {
                        heightModifier = -1
                    }
                }

                if (directions.contains(dir.rotateRight())) {
                    for (newPt in listOf(
                        point.dropY() + Point2D.from(dir) + Point2D.from(dir.rotateRight()),
                        point.dropY() + Point2D.from(dir) + Point2D.from(dir.rotateRight()) * 2,
                        point.dropY() + Point2D.from(dir) * 2 + Point2D.from(dir.rotateRight())
                    )) {
                        if (wallPoints.contains(newPt)) {
                            break
                        }
                        if (!walkwayPoints.contains(newPt)) {
                            walkwayPoints.add(newPt)
                            walkwayHeights[newPt] = point.y + heightModifier
                        }
                    }
                }

                for (x in 1..3) {
                    val newPt = point.dropY() + Point2D.from(dir) * x
                    if (wallPoints.contains(newPt)) {
                        break
                    }
                    if (!walkwayPoints.contains(newPt)) {
                        walkwayPoints.add(newPt)
                        walkwayHeights[newPt] = point.y + heightModifier
                    }
                }
            }
        }
    }

    flattenWalkway(walkwayPoints, walkwayHeights, editor, materialPlacer, materialId)
    buildWallGate(wallPointsWithHeight, editor, rng, materialPlacer, true, false, null, null, structures, 6)
}

suspend fun buildWallStandardWithInner(
    wallPoints: List<Point2D>,
    editor: Editor,
    rng: RNG,
    materialPlacer: Placer,
    materialId: MaterialId,
    structures: Map<StructureId, Structure>,
    urbanPoints: Set<Point2D>
) {
    val wallPointsWithHeight = addWallPointsHeight(wallPoints, editor)
    val enhancedWallPoints = checkWater(
        addWallPointsDirectionality(
            wallPointsWithHeight,
            wallPoints.toSet(),
            urbanPoints
        ).toMutableList(),
        editor
    )

    val walkwayPoints = mutableListOf<Point2D>()
    val walkwayHeights = mutableMapOf<Point2D, Int>()
    val innerWallPoints = mutableSetOf<Point3D>()

    var previousDir = Cardinal.NORTH

    for ((i, triple) in enhancedWallPoints.withIndex()) {
        val (point, directions, wallType) = triple
        var fillIn = false

        if (wallType == WallType.Water) {
            continue
        } else {
            if (i == 0 || i == enhancedWallPoints.size - 1 ||
                enhancedWallPoints[i + 1].third == WallType.Water ||
                enhancedWallPoints[i - 1].third == WallType.Water ||
                point.y > enhancedWallPoints[i + 1].first.y + 4 ||
                point.y > enhancedWallPoints[i - 1].first.y + 4
            ) {
                fillIn = true
            }

            if (wallType == WallType.WaterWall) {
                fillWater(point.dropY(), editor, materialPlacer, materialId)
            }

            for (y in editor.world().getHeightAt(point.dropY())..point.y) {
                val newPoint = Point3D(point.x, y, point.z)
                materialPlacer.placeBlock(editor, newPoint, materialId, BlockForm.BLOCK, null, null)
            }

            if (directions.isNotEmpty()) {
                previousDir = directions[0]
            }
            val state = mapOf("facing" to previousDir.rotateRight().toString())
            materialPlacer.placeBlock(
                editor,
                Point3D(point.x, point.y + 1, point.z),
                materialId,
                BlockForm.STAIRS,
                state,
                null
            )

            for (dir in directions) {
                var heightModifier = 0

                if (i != 0 && i != enhancedWallPoints.size - 1) {
                    val prevH = enhancedWallPoints[i - 1].first.y
                    val nextH = enhancedWallPoints[i + 1].first.y
                    val h = point.y
                    if (prevH == h - 1 && nextH == h - 1) {
                        heightModifier = -1
                    }
                }

                if (directions.contains(dir.rotateRight())) {
                    for (newPt in listOf(
                        point.dropY() + Point2D.from(dir) + Point2D.from(dir.rotateRight()),
                        point.dropY() + Point2D.from(dir) + Point2D.from(dir.rotateRight()) * 2,
                        point.dropY() + Point2D.from(dir) * 2 + Point2D.from(dir.rotateRight())
                    )) {
                        if (wallPoints.contains(newPt)) {
                            break
                        }
                        if (!walkwayPoints.contains(newPt)) {
                            walkwayPoints.add(newPt)
                            walkwayHeights[newPt] = point.y + heightModifier
                        }
                        if (fillIn) {
                            for (y in editor.world().getHeightAt(newPt) until point.y) {
                                materialPlacer.placeBlock(
                                    editor,
                                    newPt.addY(y),
                                    materialId,
                                    BlockForm.BLOCK,
                                    null,
                                    null
                                )
                            }
                            if (editor.world().isWater(newPt)) {
                                fillWater(newPt, editor, materialPlacer, materialId)
                            }
                        }
                    }

                    for (newPt in listOf(
                        point.dropY() + Point2D.from(dir) * 2 + Point2D.from(dir.rotateRight()) * 2,
                        point.dropY() + Point2D.from(dir) + Point2D.from(dir.rotateRight()) * 3,
                        point.dropY() + Point2D.from(dir) * 2 + Point2D.from(dir.rotateRight()) * 2
                    )) {
                        if (!wallPoints.contains(newPt) && !walkwayPoints.contains(newPt)) {
                            innerWallPoints.add(newPt.addY(point.y))
                        }
                    }
                }

                for (x in 1..3) {
                    val newPt = point.dropY() + Point2D.from(dir) * x
                    if (wallPoints.contains(newPt)) {
                        break
                    }
                    if (!walkwayPoints.contains(newPt)) {
                        walkwayPoints.add(newPt)
                        walkwayHeights[newPt] = point.y + heightModifier
                        if (x == 3) {
                            val innerPoint = point.dropY() + Point2D.from(dir) * 4
                            if (!wallPoints.contains(innerPoint) && !walkwayPoints.contains(innerPoint)) {
                                innerWallPoints.add(innerPoint.addY(point.y))
                            }
                        }
                    }
                    if (fillIn) {
                        for (y in editor.world().getHeightAt(newPt) until point.y) {
                            materialPlacer.placeBlock(
                                editor,
                                newPt.addY(y),
                                materialId,
                                BlockForm.BLOCK,
                                null,
                                null
                            )
                        }
                        if (editor.world().isWater(newPt)) {
                            fillWater(newPt, editor, materialPlacer, materialId)
                        }
                    }
                }
            }
        }
    }

    for (point in innerWallPoints.toList()) {
        if (!walkwayPoints.contains(point.dropY())) {
            for (y in editor.world().getHeightAt(point.dropY())..point.y) {
                materialPlacer.placeBlock(
                    editor,
                    point.dropY().addY(y),
                    materialId,
                    BlockForm.BLOCK,
                    null,
                    null
                )
            }
            if (editor.world().isWater(point.dropY())) {
                fillWater(point.dropY(), editor, materialPlacer, materialId)
            }
        } else {
            innerWallPoints.remove(point)
        }
    }

    flattenWalkway(walkwayPoints, walkwayHeights, editor, materialPlacer, materialId)
    buildWallTowers(walkwayPoints, walkwayHeights, editor, materialPlacer, materialId, structures, rng)
    buildWallGate(
        wallPointsWithHeight,
        editor,
        rng,
        materialPlacer,
        false,
        false,
        enhancedWallPoints,
        innerWallPoints,
        structures,
        6
    )
}

fun addWallPointsHeight(wallPoints: List<Point2D>, editor: Editor): List<Point3D> {
    var currentHeight = editor.world().getHeightAt(wallPoints[0])
    var targetHeight = currentHeight
    val wallHeight23 = WALL_HEIGHT * 2 / 3
    val heightWallPoints = mutableListOf<Point3D>()

    for ((i, point) in wallPoints.withIndex()) {
        if (i % 5 == 0) {
            val idx5 = if (i + 5 >= wallPoints.size - 1) 0 else i + 5
            targetHeight = editor.world().getHeightAt(wallPoints[idx5])

            if (targetHeight > currentHeight + wallHeight23 || targetHeight < currentHeight + wallHeight23) {
                val idx10 = if (i + 10 >= wallPoints.size - 1) 0 else i + 10
                targetHeight = editor.world().getHeightAt(wallPoints[idx10])
            }
        }

        val pointHeight = editor.world().getHeightAt(point)
        if (currentHeight < pointHeight - wallHeight23) {
            currentHeight = pointHeight
            targetHeight = currentHeight
        } else if (currentHeight != targetHeight && i > 1 && i < wallPoints.size - 2) {
            if (isStraightPoint2D(wallPoints[i - 2], wallPoints[i + 2], 4)) {
                when {
                    currentHeight < targetHeight -> currentHeight++
                    currentHeight > targetHeight -> currentHeight--
                }
            }
        }

        val newPoint = Point3D(point.x, currentHeight + WALL_HEIGHT, point.y)
        heightWallPoints.add(newPoint)
    }

    return heightWallPoints
}

fun addWallPointsDirectionality(
    wallPoints: List<Point3D>,
    wallSet: Set<Point2D>,
    innerPoints: Set<Point2D>
): List<Triple<Point3D, List<Cardinal>, WallType>> {
    val enhancedWallPoints = mutableListOf<Triple<Point3D, List<Cardinal>, WallType>>()

    for (point in wallPoints) {
        val directions = mutableListOf<Cardinal>()
        val neighbours = getNeighboursInSet(point.dropY(), innerPoints)

        for (neighbour in neighbours) {
            if (!wallSet.contains(neighbour)) {
                Cardinal.fromPoint2D(neighbour - point.dropY())?.let { dir ->
                    directions.add(dir)
                }
            }
        }

        enhancedWallPoints.add(Triple(point, directions, WallType.Standard))
    }

    return enhancedWallPoints
}

fun checkWater(
    wallPoints: MutableList<Triple<Point3D, List<Cardinal>, WallType>>,
    editor: Editor
): List<Triple<Point3D, List<Cardinal>, WallType>> {
    val enhancedWallPoints = wallPoints.toMutableList()

    for (i in enhancedWallPoints.indices) {
        val point = enhancedWallPoints[i].first
        if (editor.world().isWater(point.dropY())) {
            enhancedWallPoints[i] = enhancedWallPoints[i].copy(third = WallType.WaterWall)
        }
    }

    return enhancedWallPoints
}

suspend fun fillWater(
    point: Point2D,
    editor: Editor,
    materialPlacer: Placer,
    materialId: MaterialId
) {
    val waterPoints = mutableListOf<Point3D>()
    var height = editor.world().getHeightAt(point) - 1

    while (editor.world().isWater3d(point.addY(height)) && height > 0) {
        waterPoints.add(Point3D(point.x, height, point.y))
        height--
    }

    materialPlacer.placeBlocks(
        editor,
        waterPoints.toList(),
        materialId,
        BlockForm.BLOCK,
        null,
        null
    )
}

suspend fun flattenWalkway(
    walkwayPoints: List<Point2D>,
    walkwayHeights: MutableMap<Point2D, Int>,
    editor: Editor,
    materialPlacer: Placer,
    materialId: MaterialId
): Map<Point2D, Double> {
    val updatedWalkwayHeights = walkwayPoints.associateWith { point ->
        averageNeighbourHeight(point, walkwayHeights)
    }.toMutableMap()

    for ((point, height) in updatedWalkwayHeights.toMap()) {
        val fracHeight = height % 1.0
        when {
            fracHeight <= 0.25 || fracHeight > 0.75 -> {
                materialPlacer.placeBlock(
                    editor,
                    Point3D(point.x, height.roundToInt(), point.y),
                    materialId,
                    BlockForm.SLAB,
                    null,
                    null
                )
                updatedWalkwayHeights[point] = height.roundToInt().toDouble()
            }
            fracHeight in 0.25..0.5 -> {
                val state = mapOf("type" to "top")
                materialPlacer.placeBlock(
                    editor,
                    Point3D(point.x, height.roundToInt(), point.y),
                    materialId,
                    BlockForm.SLAB,
                    state,
                    null
                )
                updatedWalkwayHeights[point] = height.roundToInt() + 0.49
            }
            fracHeight in 0.5..0.75 -> {
                materialPlacer.placeBlock(
                    editor,
                    Point3D(point.x, height.roundToInt() - 1, point.y),
                    materialId,
                    BlockForm.SLAB,
                    null,
                    null
                )
                updatedWalkwayHeights[point] = height.roundToInt() - 0.51
            }
        }
    }

    for ((point, height) in updatedWalkwayHeights.toMap()) {
        val cardinals2d = Point2D.CARDINALS_2D
        for (direction in cardinals2d) {
            val neighbour = point + direction
            if (!updatedWalkwayHeights.containsKey(neighbour)) {
                continue
            } else if (height % 1.0 == 0.0) {
                if ((updatedWalkwayHeights[neighbour] ?: 0.0) - height >= 1.0) {
                    val state = mapOf(
                        "facing" to (Cardinal.fromPoint2D(direction)?.toString() ?: "")
                    )
                    materialPlacer.placeBlock(
                        editor,
                        Point3D(point.x, height.roundToInt(), point.y),
                        materialId,
                        BlockForm.STAIRS,
                        state,
                        null
                    )
                }
            } else if ((updatedWalkwayHeights[neighbour] ?: 0.0) - height <= -1.0) {
                val state = mapOf(
                    "facing" to (Cardinal.fromPoint2D(direction)?.opposite()?.toString() ?: "")
                )
                materialPlacer.placeBlock(
                    editor,
                    Point3D(point.x, height.roundToInt() + 1, point.y),
                    materialId,
                    BlockForm.STAIRS,
                    state,
                    null
                )
            }
        }
    }

    return updatedWalkwayHeights
}

fun averageNeighbourHeight(point: Point2D, walkwayHeights: Map<Point2D, Int>): Double {
    val neighbours = (-RANGE..RANGE).flatMap { x ->
        (-RANGE..RANGE).map { z ->
            Point2D(x, z)
        }
    }

    var totalHeight = 0.0
    var totalWeight = 0.0

    for (neighbour in neighbours) {
        val checkPoint = Point2D(point.x + neighbour.x, point.y + neighbour.y)
        if (!walkwayHeights.containsKey(checkPoint)) {
            continue
        } else if (abs((walkwayHeights[checkPoint] ?: 0) - (walkwayHeights[point] ?: 0)) >= 4) {
            continue
        }

        val distance = abs(neighbour.x) + abs(neighbour.y)
        val weight = 0.8.pow(distance.toDouble())
        totalHeight += (walkwayHeights[checkPoint] ?: 0).toDouble() * weight
        totalWeight += weight
    }

    return totalHeight / totalWeight
}

suspend fun buildWallTowers(
    walkwayPoints: List<Point2D>,
    walkwayHeights: Map<Point2D, Int>,
    editor: Editor,
    materialPlacer: Placer,
    materialId: MaterialId,
    structures: Map<StructureId, Structure>,
    rng: RNG
) {
    val distanceToNextTower = 80
    var towerPossible = rng.randI32Range(0, distanceToNextTower / 2)
    val tower = structures[StructureId("basic_tower")]
        ?: error("Structure not found")
    val walkwaySet = walkwayPoints.toSet()

    for (point in walkwayPoints) {
        if (towerPossible == 0) {
            if (isPointSurroundedByPoints(point, walkwaySet)) {
                towerPossible = distanceToNextTower
                val neighbours = (point.x - 2..point.x + 2).flatMap { x ->
                    (point.y - 2..point.y + 2).map { y ->
                        Point2D(x, y)
                    }
                }

                val pointHeight = walkwayHeights[point] ?: 0
                for (neighbour in neighbours) {
                    for (height in pointHeight - 1..pointHeight + 5) {
                        if (height == pointHeight + 5 || !walkwaySet.contains(neighbour)) {
                            materialPlacer.placeBlock(
                                editor,
                                neighbour.addY(height),
                                materialId,
                                BlockForm.BLOCK,
                                null,
                                null
                            )
                        }
                    }
                }

                logger.info("Placing tower at: ${point.addY(pointHeight + 6)}")
                placeStructure(
                    editor,
                    null,
                    tower,
                    point.addY(pointHeight + 6),
                    Cardinal.NORTH,
                    null,
                    null,
                    null,
                    false,
                    false
                )
            }
        } else {
            towerPossible--
        }
    }
}
