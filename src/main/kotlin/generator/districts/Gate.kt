package generator.districts

import editor.Editor
import generator.BuildClaim
import generator.materials.Placer
import generator.nbts.Structure
import generator.nbts.StructureId
import generator.nbts.placeStructure
import geometry.Cardinal
import geometry.Point2D
import geometry.Point3D
import geometry.isStraightNotDiagonalPoint2D
import minecraft.Block
import minecraft.BlockID
import noise.RNG
import org.slf4j.LoggerFactory
import kotlin.math.abs

private val logger = LoggerFactory.getLogger("Gate")

suspend fun buildWallGate(
    wallPoints: List<Point3D>,
    editor: Editor,
    rng: RNG,
    materialPlacer: Placer,
    isThin: Boolean,
    isPalisade: Boolean,
    enhancedWallPoints: List<Triple<Point3D, List<Cardinal>, WallType>>?,
    innerWallSet: Set<Point3D>?,
    structures: Map<StructureId, Structure>,
    gateHeight: Int
) {
    val distanceToNextGate = 60
    val gateSize = 7
    var gatePossible = 0
    val palisadeGate = structures[StructureId("basic_palisade_gate")]
        ?: error("Structure not found")
    val thinGate = structures[StructureId("basic_thin_gate")]
        ?: error("Structure not found")
    val wideGate = structures[StructureId("basic_wide_gate")]
        ?: error("Structure not found")

    val innerWallPoints = innerWallSet
        ?.map { it.dropY() }
        ?.toSet()
        ?: emptySet()

    val air = Block(BlockID.Air, null, null)

    for ((i, point) in wallPoints.withIndex()) {
        if (gatePossible == 0) {
            if (isGatePossible(point, wallPoints, gateSize, i)) {
                if (isPalisade) {
                    val middlePoint = Point3D(
                        wallPoints[i + 2].x,
                        editor.world().getHeightAt(wallPoints[i + 2].dropY()),
                        wallPoints[i + 2].z
                    )
                    val direction = if (point.x == wallPoints[i + 6].x) {
                        Cardinal.NORTH
                    } else {
                        Cardinal.EAST
                    }

                    val neighbours = if (direction == Cardinal.EAST) {
                        (middlePoint.x - 2..middlePoint.x + 2).flatMap { x ->
                            (middlePoint.z - 1..middlePoint.z + 1).map { z ->
                                Point2D(x, z)
                            }
                        }
                    } else {
                        (middlePoint.x - 1..middlePoint.x + 1).flatMap { x ->
                            (middlePoint.z - 2..middlePoint.z + 2).map { z ->
                                Point2D(x, z)
                            }
                        }
                    }

                    val height = middlePoint.y
                    for (neighbour in neighbours) {
                        editor.world().claim(neighbour, BuildClaim.Gate)
                        for (h in height until height + gateHeight) {
                            editor.placeBlockForced(air, neighbour.addY(h))
                        }
                    }

                    logger.info("Placing palisade gate at: $middlePoint")
                    placeStructure(
                        editor,
                        null,
                        palisadeGate,
                        middlePoint,
                        direction,
                        null,
                        null,
                        false,
                        false
                    )
                    gatePossible = distanceToNextGate
                } else if (isThin) {
                    val middlePoint = Point3D(
                        wallPoints[i + 3].x,
                        editor.world().getHeightAt(wallPoints[i + 3].dropY()),
                        wallPoints[i + 3].z
                    )
                    val direction = if (point.x == wallPoints[i + 6].x) {
                        Cardinal.NORTH
                    } else {
                        Cardinal.EAST
                    }

                    val neighbours = if (direction == Cardinal.NORTH || direction == Cardinal.SOUTH) {
                        (middlePoint.x - 3..middlePoint.x + 3).flatMap { x ->
                            (middlePoint.z - 1..middlePoint.z + 1).map { z ->
                                Point2D(x, z)
                            }
                        }
                    } else {
                        (middlePoint.x - 1..middlePoint.x + 1).flatMap { x ->
                            (middlePoint.z - 3..middlePoint.z + 3).map { z ->
                                Point2D(x, z)
                            }
                        }
                    }

                    val height = middlePoint.y
                    for (neighbour in neighbours) {
                        editor.world().claim(neighbour, BuildClaim.Gate)
                        for (h in height until height + gateHeight) {
                            editor.placeBlockForced(air, neighbour.addY(h))
                        }
                    }

                    val mirrorX = direction == Cardinal.NORTH || direction == Cardinal.SOUTH
                    logger.info("Placing thin gate at: $middlePoint")
                    placeStructure(
                        editor,
                        null,
                        thinGate,
                        middlePoint,
                        direction,
                        null,
                        null,
                        mirrorX,
                        false
                    )
                    gatePossible = distanceToNextGate
                } else {
                    val enhancedPoints = enhancedWallPoints
                        ?: error("Enhanced wall points should be provided for this wall type")
                    val direction = enhancedPoints[i + 3].second.firstOrNull()
                        ?: continue
                    val middlePoint = enhancedPoints[i + 3].first.dropY() + Point2D.from(direction) * 2

                    var canBuild = true
                    for (a in i until i + gateSize) {
                        val innerWallPoint = enhancedPoints[a].first.dropY() + Point2D.from(direction) * 5
                        if (innerWallPoints.contains(innerWallPoint)) {
                            canBuild = false
                            break
                        }
                    }

                    if (canBuild) {
                        logger.info("Building gate at $middlePoint")
                        val neighbours = (middlePoint.x - 3..middlePoint.x + 3).flatMap { x ->
                            (middlePoint.y - 3..middlePoint.y + 3).map { y ->
                                Point2D(x, y)
                            }
                        }

                        val height = editor.world().getHeightAt(middlePoint)
                        for (neighbour in neighbours) {
                            editor.world().claim(neighbour, BuildClaim.Gate)
                            for (h in height until height + gateHeight) {
                                editor.placeBlockForced(air, neighbour.addY(h))
                            }
                        }

                        val mirrorX = direction == Cardinal.NORTH || direction == Cardinal.SOUTH
                        logger.info("Placing wide gate at: $middlePoint")
                        placeStructure(
                            editor,
                            null,
                            wideGate,
                            middlePoint.addY(height),
                            direction.rotateRight(),
                            null,
                            null,
                            mirrorX,
                            false
                        )
                        gatePossible = distanceToNextGate
                    }
                }
            }
        } else {
            gatePossible--
        }
    }
}

fun isGatePossible(
    point: Point3D,
    wallList: List<Point3D>,
    gateSize: Int,
    index: Int
): Boolean {
    if (index + gateSize > wallList.size) {
        return false
    }

    return isStraightNotDiagonalPoint2D(
        Point2D(point.x, point.z),
        Point2D(wallList[index + gateSize - 1].x, wallList[index + gateSize - 1].z),
        gateSize - 1
    ) && abs(point.y - wallList[index + gateSize - 1].y) <= 1
}
