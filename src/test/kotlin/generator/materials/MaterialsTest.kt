package generator.materials

import editor.World
import geometry.Point3D
import http.GDMCHTTPProvider
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import minecraft.BlockForm
import noise.RNG
import noise.Seed
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MaterialsTest {
    private val logger = LoggerFactory.getLogger(MaterialsTest::class.java)

    @Test
    fun `deserialize material`() {
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
        """

        val material = Json { ignoreUnknownKeys = true }.decodeFromString<Material>(jsonData)

        assertEquals("test_material", material.id().asStr())
        assertEquals("lighter_material", material.more(MaterialFeature.SHADE)?.asStr())
        assertEquals("darker_material", material.less(MaterialFeature.SHADE)?.asStr())
    }

    @Test
    fun `load materials`() {
        val materials = MaterialLoader.load()
        logger.info("Loaded {} materials", materials.size)
        assert(materials.isNotEmpty())
    }

    @Test
    fun `test linear mapping`() = runBlocking {
        val provider = GDMCHTTPProvider()
        val world = World.new(provider)
        val editor = world.getEditor()
        val materials = MaterialLoader.load()
        val material = MaterialId.new("cobblestone")
        val worldRect = editor.world().worldRect2d()
        var rng = RNG.new(Seed(42))

        val placer = Placer(materials, rng).withShadeFunction { point ->
            point.x.toFloat() / worldRect.size.x.toFloat()
        }

        for (point in worldRect.asSequence()) {
            val point3d = editor.world().addHeight(point)
            placer.placeBlock(editor, point3d, material, BlockForm.BLOCK, null, null)
        }
    }

    @Test
    fun `perlin noise test`() = runBlocking {
        val provider = GDMCHTTPProvider()
        val world = World.new(provider)
        val editor = world.getEditor()
        val materials = MaterialLoader.load()
        val material = MaterialId.new("cobblestone")

        val perlin = PerlinSettings.large(Seed(42))
        var rng = RNG.new(Seed(42))

        val placer = Placer(materials, rng).withShadeFunction { point ->
            perlin.get(point) + 0.5f
        }

        val points = editor.world().worldRect2d().asSequence().map { point ->
            editor.world().addHeight(point)
        }.asIterable()

        placer.placeBlocks(editor, points, material, BlockForm.BLOCK, null, null)
    }

    @Test
    fun `gradient test`() = runBlocking {
        val provider = GDMCHTTPProvider()
        val world = World.new(provider)
        val editor = world.getEditor()
        val materials = MaterialLoader.load()
        val material = MaterialId.new("cobblestone")

        val gradient = Gradient(
            PerlinSettings.small(Seed(25)),
            1.0f,
            0.05f
        ).withX(0, editor.world().buildArea.width())

        var rng = RNG.new(Seed(42))

        val placer = Placer(materials, rng).withShadeFunction { point ->
            logger.info("Point: {}", gradient.getValue(point))
            gradient.getValue(point)
        }

        val points = editor.world().worldRect2d().asSequence().map { point ->
            editor.world().addHeight(point)
        }.asIterable()

        placer.placeBlocks(editor, points, material, BlockForm.BLOCK, null, null)
    }
}
