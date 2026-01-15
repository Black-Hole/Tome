package generator.buildings

import editor.World
import generator.buildings.roofs.buildRoof
import generator.buildings.walls.buildWalls
import generator.data.LoadedData
import generator.materials.PaletteId
import generator.style.Style
import geometry.Cardinal
import geometry.Point3D
import http.GDMCHTTPProvider
import kotlinx.coroutines.runBlocking
import minecraft.BlockID
import noise.RNG
import noise.Seed
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class GridTest {
    private val logger = LoggerFactory.getLogger(GridTest::class.java)

    @Test
    fun `grid placement test`() = runBlocking {
        logger.info("Starting grid placement test")

        val provider = GDMCHTTPProvider()
        val world = World.new(provider)
        val editor = world.getEditor()

        val data = LoadedData.load()
        val palette = PaletteId("test2")

        val midpoint = editor.worldMut().worldRect2D().size / 2
        val point = editor.worldMut().addHeight(midpoint)

        logger.info("Placing structure at: {}", point)

        val grid = Grid.new(point.toPoint3D())

        val structures = generator.nbts.Structure.load()
        val structure = structures[generator.nbts.StructureId("rotation_test")]
            ?: error("Structure not found")

        val rng = RNG(Seed(42))
        val placer = generator.materials.Placer(data.materials, rng)

        grid.buildStructure(
            editor = editor,
            placer = placer,
            structure = structure,
            gridCoordinate = Point3D(0, 0, 0),
            direction = Cardinal.NORTH,
            data = data,
            palette = palette
        )

        grid.buildStructure(
            editor = editor,
            placer = placer,
            structure = structure,
            gridCoordinate = Point3D(0, 1, 0),
            direction = Cardinal.EAST,
            data = data,
            palette = palette
        )

        grid.buildStructure(
            editor = editor,
            placer = placer,
            structure = structure,
            gridCoordinate = Point3D(0, 2, 0),
            direction = Cardinal.SOUTH,
            data = data,
            palette = palette
        )

        grid.buildStructure(
            editor = editor,
            placer = placer,
            structure = structure,
            gridCoordinate = Point3D(0, 3, 0),
            direction = Cardinal.WEST,
            data = data,
            palette = palette
        )

        logger.info("NBT structure placed successfully")

        editor.flushBuffer()
    }

    @Test
    fun `grid placement wall test`() = runBlocking {
        logger.info("Starting grid placement wall test")

        val provider = GDMCHTTPProvider()
        val world = World.new(provider)
        val editor = world.getEditor()

        val midpoint = editor.worldMut().worldRect2D().size / 2
        val point = editor.worldMut().addHeight(midpoint)

        logger.info("Placing structure at: {}", point)

        val grid = Grid.new(point.toPoint3D())

        val data = LoadedData.load()

        val walls = generator.buildings.walls.Wall.load()
        val wall = walls[generator.nbts.StructureId("japanese_wall_single_plain")]
            ?: error("Structure not found")
        val doorWall = walls[generator.nbts.StructureId("japanese_wall_single_plain_door")]
            ?: error("Structure not found")

        val rng = RNG(Seed(42))
        val placer = generator.materials.Placer(data.materials, rng)

        grid.buildStructure(
            editor = editor,
            placer = placer,
            structure = doorWall.structure,
            gridCoordinate = Point3D(0, 0, 0),
            direction = Cardinal.NORTH,
            data = data,
            palette = PaletteId("test1")
        )

        grid.buildStructure(
            editor = editor,
            placer = placer,
            structure = wall.structure,
            gridCoordinate = Point3D(0, 0, 0),
            direction = Cardinal.SOUTH,
            data = data,
            palette = PaletteId("test1")
        )

        grid.buildStructure(
            editor = editor,
            placer = placer,
            structure = wall.structure,
            gridCoordinate = Point3D(0, 0, 0),
            direction = Cardinal.EAST,
            data = data,
            palette = PaletteId("test1")
        )

        grid.buildStructure(
            editor = editor,
            placer = placer,
            structure = wall.structure,
            gridCoordinate = Point3D(0, 0, 0),
            direction = Cardinal.WEST,
            data = data,
            palette = PaletteId("test1")
        )

        logger.info("NBT structure placed successfully")

        editor.placeBlock(
            BlockID.RED_WOOL.toBlock(),
            point + geometry.NORTH * 10 + geometry.UP * 5
        )
        editor.flushBuffer()
    }
}
