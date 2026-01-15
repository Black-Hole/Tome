package geometry

import kotlin.math.abs

/**
 * Returns neighbors of a point that are in the given set.
 */
fun getNeighboursInSet(point: Point2D, points: Set<Point2D>): List<Point2D> {
    return point.neighbours().filter { it in points }
}

/**
 * Returns neighbors of a point that are not in the given set.
 */
fun getNeighboursNotInSet(point: Point2D, points: Set<Point2D>): List<Point2D> {
    return point.neighbours().filter { it !in points }
}

/**
 * Returns all points in the set that have at least one neighbor not in the set.
 */
fun getOuterPoints(points: Set<Point2D>): Set<Point2D> {
    return points.filter { point ->
        point.neighbours().any { it !in points }
    }.toSet()
}

/**
 * Returns outer points within a given distance from the edge and remaining inner points.
 */
fun getOuterAndInnerPoints(points: Set<Point2D>, distance: Int): Pair<Set<Point2D>, Set<Point2D>> {
    val outerPoints = getOuterPoints(points).toMutableSet()
    val visited = outerPoints.toMutableSet()
    val queue = mutableListOf<Pair<Point2D, Int>>()
    
    outerPoints.forEach { queue.add(it to 0) }

    while (queue.isNotEmpty()) {
        val (point, edgeDistance) = queue.removeAt(0)
        if (edgeDistance >= distance) {
            continue
        }

        for (direction in Point2D.CARDINALS) {
            val neighbour = point + direction
            if (neighbour !in points) continue
            if (neighbour in visited) continue
            
            visited.add(neighbour)
            outerPoints.add(neighbour)
            queue.add(neighbour to edgeDistance + 1)
        }
    }

    val innerPoints = points - outerPoints

    return outerPoints to innerPoints
}

/**
 * Checks if two points form a straight line (including diagonal) of the given length.
 */
fun isStraightPoint2D(first: Point2D, second: Point2D, length: Int): Boolean {
    val line = first - second
    return ((abs(line.x) == length || abs(line.y) == length) && (line.x == 0 || line.y == 0)) ||
           (abs(line.x) == length && abs(line.y) == length)
}

/**
 * Checks if two points form a straight non-diagonal line of the given length.
 */
fun isStraightNotDiagonalPoint2D(first: Point2D, second: Point2D, length: Int): Boolean {
    val line = first - second
    return (abs(line.x) == length || abs(line.y) == length) && (line.x == 0 || line.y == 0)
}

/**
 * Checks if a point is completely surrounded by other points (including diagonals).
 */
fun isPointSurroundedByPoints(point: Point2D, points: Set<Point2D>): Boolean {
    return Point2D.ALL_8.all { direction ->
        val neighbour = point + direction
        neighbour in points
    }
}

/**
 * Returns all points within the given distance from the edge of the set.
 */
fun getSurroundingSet(points: Set<Point2D>, distance: Int): Set<Point2D> {
    if (distance == 0) {
        return emptySet()
    }

    val surrounding = points.flatMap { point ->
        point.neighbours()
    }.filter { it !in points }.toSet()

    return if (distance == 1) {
        surrounding
    } else {
        surrounding + getSurroundingSet(surrounding, distance - 1)
    }
}
