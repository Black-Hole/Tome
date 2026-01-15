package generator.paths

import editor.World
import generator.materials.MaterialId
import geometry.Point3D
import http.GDMCHTTPProvider
import kotlinx.coroutines.test.runTest
import minecraft.Block
import minecraft.BlockID
import noise.RNG
import noise.Seed
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import kotlin.system.measureTimeMillis
import kotlin.math.absoluteValue

class PathsTest {
    private val logger = LoggerFactory.getLogger(PathsTest::class.java)
    
    @Test
    fun `test A-star algorithm`() = runTest {
        val target = Pair(100, 100)
        
        val duration = measureTimeMillis {
            val path = aStar(
                start = listOf(Pair(0, 0)),
                isEnd = { node -> node.last() == target },
                neighbors = { node ->
                    val (x, y) = node.last()
                    listOf(
                        Pair(x + 1, y), // Right
                        Pair(x - 1, y), // Left
                        Pair(x, y + 1), // Down
                        Pair(x, y - 1)  // Up
                    ).map { pos ->
                        node + pos
                    }
                },
                cost = { prevCost, node ->
                    prevCost + 1u
                },
                heuristic = { node ->
                    val (x, y) = node.last()
                    ((target.first - x).absoluteValue + (target.second - y).absoluteValue).toULong()
                },
                exploreNodeCallback = {}
            )
            
            logger.info("Path found: {}", path)
        }
        
        logger.info("A* search took: {} ms", duration)
    }
    
    @Test
    fun `test route path`() = runTest {
        val world = World.new(GDMCHTTPProvider())
        val editor = world.getEditor()
        
        val editor2 = World.new(GDMCHTTPProvider()).getEditor()
        editor2.setBufferSize(1)
        
        val rect = editor.world().worldRect2d()
        
        val start = editor.world().addHeight(rect.origin)
        val end = editor.world().addHeight(rect.last())
        
        val path = routePath(editor, start, end) { points: List<Point3D> ->
            editor2.placeBlock(Block.from(BlockID.PINK_WOOL), points.last())
        }
        
        if (path != null) {
            for (point in path) {
                editor.placeBlock(Block.from(BlockID.RED_WOOL), point)
            }
        }
        
        editor.flushBuffer()
    }
    
    @Test
    fun `test build path`() = runTest {
        val world = World.new(GDMCHTTPProvider())
        val editor = world.getEditor()
        
        val rect = editor.world().worldRect2d()
        
        val start = editor.world().addHeight(rect.origin)
        val end = editor.world().addHeight(rect.last())
        
        val path = getPath(
            editor,
            start,
            end,
            PathPriority.MEDIUM,
            MaterialId.new("cobblestone")
        ) {}
        
        if (path != null) {
            val rng = RNG(Seed(42))
            buildPath(editor, path, rng)
        }
        
        editor.flushBuffer()
    }
}
