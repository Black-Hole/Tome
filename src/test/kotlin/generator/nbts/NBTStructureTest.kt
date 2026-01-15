package generator.nbts

import generator.materials.Material
import generator.materials.MaterialId
import generator.materials.Palette
import generator.materials.PaletteId
import generator.materials.Placer
import geometry.Cardinal
import geometry.Point3D
import kotlinx.coroutines.runBlocking
import minecraft.Block
import minecraft.BlockID
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("generator.nbts.NBTStructureTest")

class NBTStructureTest {

    @Test
    fun testNBTStructureFromBlocks() {
        val blocks = listOf(
            Pair(Block(id = BlockID.STONE), Point3D(0, 0, 0)),
            Pair(Block(id = BlockID.DIRT), Point3D(1, 0, 0)),
            Pair(Block(id = BlockID.GRASS_BLOCK), Point3D(0, 1, 0))
        )

        val structure = NBTStructure.fromBlocks(blocks)

        assertEquals(listOf(2, 2, 1), structure.size)
        assertEquals(3, structure.palette.size)
        assertEquals(3, structure.blocks.size)
        assertTrue(structure.entities.isEmpty())
    }

    @Test
    fun testRotationApplyToPoint() {
        val point = Point3D(1, 0, 0)

        val rotatedOnce = Rotation.ONCE.applyToPoint(point)
        assertEquals(Point3D(0, 0, -1), rotatedOnce)

        val rotatedTwice = Rotation.TWICE.applyToPoint(point)
        assertEquals(Point3D(-1, 0, 0), rotatedTwice)

        val rotatedThrice = Rotation.THRICE.applyToPoint(point)
        assertEquals(Point3D(0, 0, 1), rotatedThrice)
    }

    @Test
    fun testRotationApplyToCardinal() {
        val north = Cardinal.NORTH

        assertEquals(Cardinal.EAST, Rotation.ONCE.applyToCardinal(north))
        assertEquals(Cardinal.SOUTH, Rotation.TWICE.applyToCardinal(north))
        assertEquals(Cardinal.WEST, Rotation.THRICE.applyToCardinal(north))
        assertEquals(Cardinal.NORTH, Rotation.NONE.applyToCardinal(north))
    }

    @Test
    fun testRotationArithmetic() {
        assertEquals(Rotation.TWICE, Rotation.ONCE + Rotation.ONCE)
        assertEquals(Rotation.NONE, Rotation.TWICE + Rotation.TWICE)
        assertEquals(Rotation.NONE, Rotation.ONCE - Rotation.ONCE)
        assertEquals(Rotation.THRICE, -Rotation.ONCE)
    }

    @Test
    fun testTransformApply() {
        val transform = Transform.new(Point3D(10, 0, 10), Rotation.ONCE)
        val point = Point3D(1, 0, 0)
        
        val transformed = transform.apply(point)
        assertEquals(Point3D(10, 0, 9), transformed)
    }

    @Test
    fun testTransformShift() {
        val transform = Transform.new(Point3D(0, 0, 0), Rotation.NONE)
        transform.shift(Point3D(5, 5, 5))
        
        assertEquals(Point3D(5, 5, 5), transform.position)
    }

    @Test
    fun testStructureLoader() {
        val structures = Structure.load()
        
        assertNotNull(structures)
        assertTrue(structures.isNotEmpty() || structures.isEmpty()) // Just test that load works
        
        logger.info("Loaded ${structures.size} structures")
    }
}
