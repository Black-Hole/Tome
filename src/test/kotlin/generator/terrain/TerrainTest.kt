package generator.terrain

import editor.Editor
import editor.World
import geometry.Point2D
import geometry.Point3D
import generator.materials.Material
import generator.materials.MaterialFeature
import http.GDMCHTTPProvider
import kotlinx.coroutines.runBlocking
import noise.RNG
import noise.Seed
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("TerrainTest")

class TerrainTest {

    @Test
    fun `test deserialize material`() {
        val jsonData = """
            {
                "id": "test_material",
                "connections": {
                    "lighter": "lighter_material",
                    "darker": "darker_material",
                    "less_worn": "less_worn_material",
                    "more_decorated": "more_decorated_material",
                    "less_decorated": "less_decorated_material"
                },
                "blocks": {
                    "block": "minecraft:stone"
                }
            }
        """.trimIndent()

        val material = data.Loadable.json.decodeFromString(Material.serializer(), jsonData)

        assertEquals("test_material", material.id())
        assertEquals("lighter_material", material.more(MaterialFeature.SHADE)?.asStr())
        assertEquals("darker_material", material.less(MaterialFeature.SHADE)?.asStr())
    }

    @Test
    fun `test load forests`() {
        val forests = Forest.load()
        
        assertTrue(forests.isNotEmpty(), "Should load at least one forest")
        logger.info("Loaded {} forests", forests.size)
        
        forests.forEach { (id, forest) ->
            assertNotNull(forest.id())
            assertNotNull(forest.trees())
            assertNotNull(forest.treePalette())
            assertTrue(forest.treeDensity() >= 0)
        }
    }

    @Test
    fun `test tree enum serialization`() {
        val treeJson = """"small_birch""""
        val tree = data.Loadable.json.decodeFromString(Tree.serializer(), treeJson)
        assertEquals(Tree.SMALL_BIRCH, tree)
    }

    // Note: The following tests require a running GDMC HTTP server
    // and are commented out by default. Uncomment to run with a live server.
    
    /*
    @Test
    fun `test build forest`() = runBlocking {
        val seed = Seed(12345)
        val rng = RNG(seed)
        val provider = GDMCHTTPProvider()
        val buildArea = provider.getBuildArea()
        val world = World(provider)
        val editor = Editor(buildArea, world)

        val forests = Forest.load()
        val forestId = ForestId.new("birch_forest")
        val forest = forests[forestId] ?: error("Failed to get birch forest")
        val points = editor.worldMut().worldRect2D().toSet()

        // plantForest would need to be implemented
        // plantForest(points, forest, rng, editor, null, true)
    }

    @Test
    fun `test tree line generation`() = runBlocking {
        val seed = Seed(12345)
        val rng = RNG(seed)

        val provider = GDMCHTTPProvider()
        val buildArea = provider.getBuildArea()
        val world = World(provider)
        val editor = Editor(buildArea, world)

        val palette = mapOf(
            "wood" to mapOf(
                "minecraft:birch_wood" to 5.0f,
                "minecraft:stripped_birch_wood" to 2.0f,
                "minecraft:stripped_oak_wood" to 1.0f
            ),
            "leaves" to mapOf(
                "minecraft:oak_leaves[persistent=true]" to 1.0f,
                "minecraft:acacia_leaves[persistent=true]" to 2.0f,
                "minecraft:birch_leaves[persistent=true]" to 5.0f
            )
        )

        generateTree(Tree.SMALL_BIRCH, editor, Point3D(100, 0, 0), rng, palette)
        generateTree(Tree.MEDIUM_BIRCH, editor, Point3D(100, 0, 10), rng, palette)
        generateTree(Tree.LARGE_BIRCH, editor, Point3D(100, 0, 20), rng, palette)
        generateTree(Tree.MEGA_BIRCH, editor, Point3D(100, 0, 30), rng, palette)
        generateTree(Tree.SMALL_HEDGE, editor, Point3D(100, 0, 40), rng, palette)
        generateTree(Tree.MEDIUM_HEDGE, editor, Point3D(100, 0, 50), rng, palette)
        generateTree(Tree.LARGE_HEDGE, editor, Point3D(100, 0, 60), rng, palette)
        generateTree(Tree.MEGA_HEDGE, editor, Point3D(100, 0, 70), rng, palette)
        generateTree(Tree.SMALL_OAK, editor, Point3D(100, 0, 80), rng, palette)
        generateTree(Tree.MEDIUM_OAK, editor, Point3D(100, 0, 90), rng, palette)
        generateTree(Tree.LARGE_OAK, editor, Point3D(100, 0, 100), rng, palette)
        generateTree(Tree.MEGA_OAK, editor, Point3D(100, 0, 110), rng, palette)
        generateTree(Tree.SMALL_PINE, editor, Point3D(100, 0, 120), rng, palette)
        generateTree(Tree.MEDIUM_PINE, editor, Point3D(100, 0, 130), rng, palette)
        generateTree(Tree.LARGE_PINE, editor, Point3D(100, 0, 140), rng, palette)
        generateTree(Tree.MEGA_PINE, editor, Point3D(100, 0, 150), rng, palette)

        editor.flushBuffer()
    }

    @Test
    fun `test cut trees`() = runBlocking {
        val provider = GDMCHTTPProvider()
        val buildArea = provider.getBuildArea()
        val world = World(provider)
        val editor = world.getEditor()
        val points = mutableSetOf<Point2D>()

        for (x in 0 until buildArea.size.x) {
            for (z in 0 until buildArea.size.z) {
                points.add(Point2D(x, z))
            }
        }

        logTrees(editor, points)
        editor.flushBuffer()
    }
    */
}
