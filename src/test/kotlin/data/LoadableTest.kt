package data

import kotlinx.serialization.Serializable
import kotlinx.serialization.DeserializationStrategy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

@Serializable
data class TestPalette(
    val id: String,
    val primary_wall: String? = null,
    val primary_wood: String? = null
)

class TestPaletteLoader : Loadable<TestPalette, String> {
    override fun getKey(item: TestPalette): String = item.id
    override fun path(): String = "palettes"
    override fun deserializer(): DeserializationStrategy<TestPalette> = TestPalette.serializer()
}

class LoadableTest {
    
    @Test
    fun testLoadableInterfaceWithRealData() {
        val loader = TestPaletteLoader()
        val palettes = loader.load()
        
        // Verify that we loaded some palettes
        assertTrue(palettes.isNotEmpty(), "Should load at least one palette")
        
        // Verify that palettes have IDs
        palettes.forEach { (key, palette) ->
            assertEquals(key, palette.id, "Key should match palette ID")
            assertNotNull(palette.id, "Palette should have an ID")
        }
        
        println("Loaded ${palettes.size} palettes")
        palettes.keys.take(5).forEach { println("  - $it") }
    }
    
    @Test
    fun testLoadableWithCustomTempDirectory(@TempDir tempDir: Path) {
        // Create test data structure
        val dataDir = tempDir.resolve("data/palettes").toFile()
        dataDir.mkdirs()
        
        // Create test JSON file
        val testFile = dataDir.resolve("test_palette.json")
        testFile.writeText("""
            {
                "id": "test_palette",
                "primary_wall": "stone",
                "primary_wood": "oak_planks"
            }
        """.trimIndent())
        
        // Create nested directory structure
        val nestedDir = dataDir.resolve("medieval")
        nestedDir.mkdirs()
        val nestedFile = nestedDir.resolve("medieval_test.json")
        nestedFile.writeText("""
            {
                "id": "medieval_test",
                "primary_wall": "cobblestone"
            }
        """.trimIndent())
        
        // Change working directory temporarily
        val originalUserDir = System.getProperty("user.dir")
        try {
            System.setProperty("user.dir", tempDir.toString())
            
            val loader = TestPaletteLoader()
            val palettes = loader.load()
            
            // Verify both files were loaded
            assertEquals(2, palettes.size, "Should load 2 palettes")
            assertTrue(palettes.containsKey("test_palette"))
            assertTrue(palettes.containsKey("medieval_test"))
            
            // Verify content
            assertEquals("stone", palettes["test_palette"]?.primary_wall)
            assertEquals("oak_planks", palettes["test_palette"]?.primary_wood)
            assertEquals("cobblestone", palettes["medieval_test"]?.primary_wall)
        } finally {
            System.setProperty("user.dir", originalUserDir)
        }
    }
    
    @Test
    fun testLoadableHandlesMalformedJsonGracefully(@TempDir tempDir: Path) {
        val dataDir = tempDir.resolve("data/palettes").toFile()
        dataDir.mkdirs()
        
        // Create valid JSON
        dataDir.resolve("valid.json").writeText("""
            {"id": "valid_palette"}
        """.trimIndent())
        
        // Create invalid JSON
        dataDir.resolve("invalid.json").writeText("""
            {invalid json}
        """.trimIndent())
        
        val originalUserDir = System.getProperty("user.dir")
        try {
            System.setProperty("user.dir", tempDir.toString())
            
            val loader = TestPaletteLoader()
            val palettes = loader.load()
            
            // Should only load the valid file
            assertEquals(1, palettes.size)
            assertTrue(palettes.containsKey("valid_palette"))
        } finally {
            System.setProperty("user.dir", originalUserDir)
        }
    }
}
