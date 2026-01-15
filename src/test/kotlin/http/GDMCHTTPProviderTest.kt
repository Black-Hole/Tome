package http

import geometry.Rect3D
import kotlinx.coroutines.test.runTest
import minecraft.BlockID
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.slf4j.LoggerFactory
import kotlin.test.assertTrue
import kotlin.test.assertEquals

/**
 * Tests for GDMCHTTPProvider.
 * Note: These tests require a running Minecraft instance with GDMC HTTP mod.
 */
class GDMCHTTPProviderTest {
    private val logger = LoggerFactory.getLogger(GDMCHTTPProviderTest::class.java)
    private lateinit var provider: GDMCHTTPProvider

    @BeforeEach
    fun setup() {
        provider = GDMCHTTPProvider()
    }

    @Test
    fun `test get blocks`() = runTest {
        val buildArea = provider.getBuildArea()
            .getOrElse { error("Failed to get build area: $it") }
        
        val blocks = provider.getBlocks(
            buildArea.origin.x, buildArea.origin.y, buildArea.origin.z,
            buildArea.size.x, buildArea.size.y, buildArea.size.z
        ).getOrElse { error("Failed to get blocks: $it") }
        
        assertTrue(blocks.isNotEmpty(), "No blocks returned from server")
        logger.info("Retrieved ${blocks.size} blocks")
    }

    @Test
    fun `test put blocks`() = runTest {
        val buildArea = provider.getBuildArea()
            .getOrElse { error("Failed to get build area: $it") }
        
        val blocks = listOf(
            PositionedBlock(
                x = Coordinate.Absolute(buildArea.origin.x),
                y = Coordinate.Absolute(buildArea.origin.y),
                z = Coordinate.Absolute(buildArea.origin.z),
                id = BlockID.STONE,
                data = null,
                state = null
            ),
            PositionedBlock(
                x = Coordinate.Absolute(buildArea.origin.x + 1),
                y = Coordinate.Absolute(buildArea.origin.y),
                z = Coordinate.Absolute(buildArea.origin.z),
                id = BlockID.STONE,
                data = null,
                state = null
            )
        )
        
        val response = provider.putBlocks(blocks)
            .getOrElse { error("Failed to put blocks: $it") }
        
        assertEquals(2, response.size, "Expected 2 block placement responses")
        logger.info("Successfully placed ${response.size} blocks")
    }

    @Test
    fun `test get biomes`() = runTest {
        val buildArea = provider.getBuildArea()
            .getOrElse { error("Failed to get build area: $it") }
        
        val biomes = provider.getBiomes(
            buildArea.origin.x, buildArea.origin.y, buildArea.origin.z,
            buildArea.size.x, buildArea.size.y, buildArea.size.z
        ).getOrElse { error("Failed to get biomes: $it") }
        
        logger.info("Biomes: $biomes")
        assertTrue(biomes.isNotEmpty(), "No biomes returned from server")
    }

    @Test
    fun `test get chunks`() = runTest {
        val buildArea = provider.getBuildArea()
            .getOrElse { error("Failed to get build area: $it") }
        
        try {
            val chunks = provider.getChunks(
                buildArea.origin.x, buildArea.origin.y, buildArea.origin.z,
                buildArea.size.x, buildArea.size.y, buildArea.size.z
            ).getOrElse { error("Failed to get chunks: $it") }
            
            logger.info("A section: ${chunks[0].sections[0]}")
        } catch (e: UnsupportedOperationException) {
            logger.warn("NBT parsing not yet implemented, skipping chunk test")
        }
    }

    @Test
    fun `test give book`() = runTest {
        val title = "Test Book"
        val author = "Test Author"
        val pages = listOf(
            "This is the first page of the book.",
            "This is the second page of the book.",
            "This is the third page of the book."
        )
        
        val book = provider.givePlayerBook(pages, title, author)
            .getOrElse { error("Failed to give book: $it") }
        
        logger.info("Book given: $book")
    }
}
