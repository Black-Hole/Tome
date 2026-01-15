package generator.buildings.walls

import editor.World
import generator.buildings.*
import generator.buildings.roofs.buildRoof
import generator.data.LoadedData
import generator.materials.PaletteId
import generator.style.Style
import geometry.Point3D
import http.GDMCHTTPProvider
import kotlinx.coroutines.runBlocking
import minecraft.BlockID
import noise.RNG
import noise.Seed
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class WallTest {
    private val logger = LoggerFactory.getLogger(WallTest::class.java)

    @Test
    fun `test build walls`() = runBlocking {
        logger.info("Starting build walls test")

        val world = World.new(GDMCHTTPProvider())
        val editor = world.getEditor()

        val data = LoadedData.load()
        val palette = PaletteId("desert_prismarine")

        val shape = BuildingShape(
            cells = listOf(
                // Base layer
                Point3D(0, 0, 0)
            ),
            stairs = listOf()
        )

        val midpoint = editor.worldMut().worldRect2D().size / 2
        val point = editor.worldMut().addHeight(midpoint)

        val grid = Grid.new(point.toPoint3D())

        val walls = data.walls
        val building = BuildingData(
            id = BuildingID.from(0),
            shape = shape,
            grid = grid,
            palette = palette,
            style = Style.DESERT
        )

        for (cell in building.shape.cells()) {
            val midpointCell = building.grid.gridToWorld(cell) + building.grid.cellSize / 2
            editor.placeBlock(BlockID.RED_MUSHROOM_BLOCK.toBlock(), midpointCell)
        }

        val rng = RNG(Seed(100))

        buildWalls(editor, walls.values.toList(), building, data, rng)
            .getOrThrow()
        buildRoof(editor, data, building, rng)
            .getOrThrow()
        buildFloor(editor, data, building, rng)
        buildStairs(editor, building, data, rng)

        editor.flushBuffer()
    }
}
