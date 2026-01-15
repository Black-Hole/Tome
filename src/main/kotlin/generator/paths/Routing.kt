package generator.paths

import editor.Editor
import generator.materials.MaterialId
import geometry.Point2D
import geometry.Point3D
import kotlin.math.absoluteValue

private fun mod4Point(point: Point3D, editor: Editor): Point3D {
    val point2d = Point2D(
        x = point.x - point.x.mod(4),
        y = point.z - point.z.mod(4)
    )
    
    return editor.world().addHeight(point2d)
}

private fun getBestMod4Point(point: Point3D, editor: Editor): Point3D {
    return listOf(
        Pair(0, 0),
        Pair(0, 4),
        Pair(4, 0),
        Pair(4, 4)
    )
        .map { (dx, dz) ->
            Point3D(
                x = point.x + dx,
                y = point.y,
                z = point.z + dz
            )
        }
        .filter { p -> editor.world().isInBounds2d(p.dropY()) }
        .map { p -> mod4Point(p, editor) }
        .minByOrNull { p -> (p.y - point.y).absoluteValue }
        ?: mod4Point(point, editor)
}

suspend fun getPath(
    editor: Editor,
    start: Point3D,
    end: Point3D,
    priority: PathPriority,
    material: MaterialId,
    exploreCallback: suspend (List<Point3D>) -> Unit
): Path? {
    val newStart = getBestMod4Point(start, editor)
    val newEnd = getBestMod4Point(end, editor)
    
    val width = when (priority) {
        PathPriority.LOW -> 1
        PathPriority.MEDIUM -> 2
        PathPriority.HIGH -> 3
    }
    
    var path = routePath(editor, newStart, newEnd, exploreCallback) ?: return null
    
    if (path.isNotEmpty()) {
        path = fillOutPath(path, priority != PathPriority.LOW)
    }
    
    return Path(
        points = path,
        width = width,
        material = material,
        priority = priority
    )
}

suspend fun routePath(
    editor: Editor,
    start: Point3D,
    end: Point3D,
    exploreCallback: suspend (List<Point3D>) -> Unit
): List<Point3D>? {
    val newStart = getBestMod4Point(start, editor)
    val newEnd = getBestMod4Point(end, editor)
    
    val heuristicWeight = 10u
    
    val getNeighbours4 = { points: List<Point3D> ->
        val neighbours = mutableListOf<Point3D>()
        val point = points.last()
        
        for (direction in Point2D.ALL_8) {
            val neighbour2d = point.dropY() + direction * 4
            
            if (editor.world().isInBounds2d(neighbour2d)) {
                var neighbour = editor.world().addHeight(neighbour2d)
                
                if ((point.y - neighbour.y).absoluteValue <= 4) {
                    neighbour = Point3D(
                        neighbour.x,
                        neighbour.y.coerceIn(point.y - 2, point.y + 2),
                        neighbour.z
                    )
                    neighbours.add(neighbour)
                    continue
                }
            }
            
            val neighbour2d2 = point.dropY() + direction * 2
            
            if (editor.world().isInBounds2d(neighbour2d2)) {
                var neighbour = editor.world().addHeight(neighbour2d2)
                
                if ((point.y - neighbour.y).absoluteValue <= 2) {
                    neighbour = Point3D(
                        neighbour.x,
                        neighbour.y.coerceIn(point.y - 2, point.y + 2),
                        neighbour.z
                    )
                    neighbours.add(neighbour)
                    continue
                }
            }
        }
        
        neighbours.map { newPoint ->
            points + newPoint
        }
    }
    
    val getCost = { prevCost: ULong, points: List<Point3D> ->
        if (points.size < 2) {
            0u
        } else {
            val last = points[points.size - 1]
            val prev = points[points.size - 2]
            var cost = prevCost + last.distance(prev).toULong()
            
            if (points.size >= 3) {
                val prevPrev = points[points.size - 3]
                val wobble = (last - prev).distance(prev - prevPrev).toULong()
                cost += wobble
            }
            
            val burrowingCost = (editor.world().getHeightAt(last.dropY()) - last.y).absoluteValue.toULong() * 10u
            cost += burrowingCost
            
            val heightDiff = (last.y - newEnd.y).absoluteValue.toULong()
            cost += heightDiff * 3u
            
            if (editor.world().isWater(last.dropY())) {
                cost += 30u
            }
            
            cost
        }
    }
    
    val getHeuristic = { points: List<Point3D> ->
        if (points.isEmpty()) {
            0u
        } else {
            val last = points.last()
            last.distance(newEnd).toULong() * heuristicWeight
        }
    }
    
    val isEnd = { points: List<Point3D> ->
        if (points.isEmpty()) {
            false
        } else {
            val last = points.last()
            last.dropY() == newEnd.dropY() && (last.y - newEnd.y).absoluteValue <= 4
        }
    }
    
    return aStar(
        start = listOf(newStart),
        isEnd = isEnd,
        neighbors = getNeighbours4,
        cost = getCost,
        heuristic = getHeuristic,
        exploreNodeCallback = exploreCallback
    )
}

fun fillOutPath(points: List<Point3D>, allowDiagonals: Boolean): List<Point3D> {
    if (points.isEmpty()) {
        return emptyList()
    }
    
    val mutablePoints = points.toMutableList()
    var currPoint = mutablePoints.removeAt(0)
    val fullPoints = mutableListOf(currPoint)
    
    if (mutablePoints.isEmpty()) {
        return fullPoints
    }
    
    var nextPoint = mutablePoints.removeAt(0)
    var xAxisFirst = true
    var canUpdateY = true
    
    while (mutablePoints.isNotEmpty() || currPoint != nextPoint) {
        if (canUpdateY) {
            when {
                currPoint.y < nextPoint.y -> {
                    currPoint = Point3D(currPoint.x, currPoint.y + 1, currPoint.z)
                    canUpdateY = false
                }
                currPoint.y > nextPoint.y -> {
                    currPoint = Point3D(currPoint.x, currPoint.y - 1, currPoint.z)
                    canUpdateY = false
                }
            }
        } else {
            canUpdateY = true
        }
        
        if (allowDiagonals) {
            when {
                currPoint.x > nextPoint.x && currPoint.z > nextPoint.z -> {
                    currPoint = Point3D(currPoint.x - 1, currPoint.y, currPoint.z - 1)
                    fullPoints.add(currPoint)
                    continue
                }
                currPoint.x < nextPoint.x && currPoint.z < nextPoint.z -> {
                    currPoint = Point3D(currPoint.x + 1, currPoint.y, currPoint.z + 1)
                    fullPoints.add(currPoint)
                    continue
                }
                currPoint.x > nextPoint.x && currPoint.z < nextPoint.z -> {
                    currPoint = Point3D(currPoint.x - 1, currPoint.y, currPoint.z + 1)
                    fullPoints.add(currPoint)
                    continue
                }
                currPoint.x < nextPoint.x && currPoint.z > nextPoint.z -> {
                    currPoint = Point3D(currPoint.x + 1, currPoint.y, currPoint.z - 1)
                    fullPoints.add(currPoint)
                    continue
                }
            }
        }
        
        if (xAxisFirst) {
            when {
                currPoint.x < nextPoint.x -> {
                    currPoint = Point3D(currPoint.x + 1, currPoint.y, currPoint.z)
                    fullPoints.add(currPoint)
                    xAxisFirst = !xAxisFirst
                    continue
                }
                currPoint.x > nextPoint.x -> {
                    currPoint = Point3D(currPoint.x - 1, currPoint.y, currPoint.z)
                    fullPoints.add(currPoint)
                    xAxisFirst = !xAxisFirst
                    continue
                }
            }
        }
        
        when {
            currPoint.z < nextPoint.z -> {
                currPoint = Point3D(currPoint.x, currPoint.y, currPoint.z + 1)
                fullPoints.add(currPoint)
                xAxisFirst = !xAxisFirst
                continue
            }
            currPoint.z > nextPoint.z -> {
                currPoint = Point3D(currPoint.x, currPoint.y, currPoint.z - 1)
                fullPoints.add(currPoint)
                xAxisFirst = !xAxisFirst
                continue
            }
            currPoint.x < nextPoint.x -> {
                currPoint = Point3D(currPoint.x + 1, currPoint.y, currPoint.z)
                fullPoints.add(currPoint)
                xAxisFirst = !xAxisFirst
                continue
            }
            currPoint.x > nextPoint.x -> {
                currPoint = Point3D(currPoint.x - 1, currPoint.y, currPoint.z)
                fullPoints.add(currPoint)
                xAxisFirst = !xAxisFirst
                continue
            }
        }
        
        // currPoint must be equal to nextPoint
        fullPoints.add(currPoint)
        if (mutablePoints.isNotEmpty()) {
            nextPoint = mutablePoints.removeAt(0)
        } else {
            break
        }
    }
    
    return fullPoints
}
