package editor

import geometry.Point2D
import geometry.Point3D
import http.GDMCHTTPProvider
import kotlinx.coroutines.runBlocking
import minecraft.Biome
import minecraft.Block
import minecraft.BlockID
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.slf4j.LoggerFactory

class EditorTest {
    private val logger = LoggerFactory.getLogger(EditorTest::class.java)

    @Test
    fun `place blocks`() = runBlocking {
        val provider = GDMCHTTPProvider()
        val buildArea = provider.getBuildArea().getOrThrow()
        
        val world = World.new(provider)
        val editor = world.getEditor()

        val block = Block(
            id = BlockID.STONE,
            data = null,
            state = null
        )

        for (x in 0 until buildArea.length()) {
            for (z in 0 until buildArea.width()) {
                val point = editor.worldMut().addHeight(Point2D(x, z))
                logger.info("Placing block at: {}", point)
                editor.placeBlock(block, point)
            }
        }
        
        editor.close()
    }

    @Test
    fun `get surface biome at`() = runBlocking {
        val provider = GDMCHTTPProvider()
        val buildArea = provider.getBuildArea().getOrThrow()
        val world = World.new(provider)

        for (x in 0 until buildArea.length()) {
            for (z in 0 until buildArea.width()) {
                val biome = world.getSurfaceBiomeAt(Point2D(x, z))
                assertNotEquals(Biome.UNKNOWN, biome, "Biome should not be unknown")
            }
        }
    }

    @Test
    fun `world get block`() = runBlocking {
        val provider = GDMCHTTPProvider()
        val world = World.new(provider)

        val block = world.getBlock(Point3D(0, 0, 0))

        println("Block at (0, 0, 0): $block")
    }

    @Test
    fun `get surface block at`() = runBlocking {
        val provider = GDMCHTTPProvider()
        val buildArea = provider.getBuildArea().getOrThrow()
        val world = World.new(provider)

        println("Build area: $buildArea")

        for (x in 0 until buildArea.length()) {
            for (z in 0 until buildArea.width()) {
                val height = world.getHeightAt(Point2D(x, z)) - 1
                val block = world.getBlock(Point3D(x, height, z))
                val point = Point3D(x, height, z) + world.buildArea.origin
                println("Block at ($point) height:$height $block")
            }
        }
    }
}
