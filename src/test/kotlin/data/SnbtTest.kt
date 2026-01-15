package data

import net.kyori.adventure.nbt.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class SnbtTest {
    
    @Test
    fun testByteTagToSnbt() {
        val tag = ByteBinaryTag.byteBinaryTag(42)
        assertEquals("42b", Snbt.toSnbt(tag))
    }
    
    @Test
    fun testShortTagToSnbt() {
        val tag = ShortBinaryTag.shortBinaryTag(1000)
        assertEquals("1000s", Snbt.toSnbt(tag))
    }
    
    @Test
    fun testIntTagToSnbt() {
        val tag = IntBinaryTag.intBinaryTag(12345)
        assertEquals("12345", Snbt.toSnbt(tag))
    }
    
    @Test
    fun testLongTagToSnbt() {
        val tag = LongBinaryTag.longBinaryTag(9876543210L)
        assertEquals("9876543210L", Snbt.toSnbt(tag))
    }
    
    @Test
    fun testFloatTagToSnbt() {
        val tag = FloatBinaryTag.floatBinaryTag(3.14f)
        assertEquals("3.14f", Snbt.toSnbt(tag))
    }
    
    @Test
    fun testDoubleTagToSnbt() {
        val tag = DoubleBinaryTag.doubleBinaryTag(2.71828)
        assertEquals("2.71828d", Snbt.toSnbt(tag))
    }
    
    @Test
    fun testStringTagToSnbt() {
        val tag = StringBinaryTag.stringBinaryTag("Hello, World!")
        assertEquals("\"Hello, World!\"", Snbt.toSnbt(tag))
    }
    
    @Test
    fun testByteArrayToSnbt() {
        val tag = ByteArrayBinaryTag.byteArrayBinaryTag(1, 2, 3, 4, 5)
        assertEquals("[B;1b,2b,3b,4b,5b]", Snbt.toSnbt(tag))
    }
    
    @Test
    fun testIntArrayToSnbt() {
        val tag = IntArrayBinaryTag.intArrayBinaryTag(10, 20, 30)
        assertEquals("[I;10,20,30]", Snbt.toSnbt(tag))
    }
    
    @Test
    fun testLongArrayToSnbt() {
        val tag = LongArrayBinaryTag.longArrayBinaryTag(100L, 200L, 300L)
        assertEquals("[L;100L,200L,300L]", Snbt.toSnbt(tag))
    }
    
    @Test
    fun testListTagToSnbt() {
        val tag = ListBinaryTag.listBinaryTag(
            BinaryTagTypes.INT,
            listOf(
                IntBinaryTag.intBinaryTag(1),
                IntBinaryTag.intBinaryTag(2),
                IntBinaryTag.intBinaryTag(3)
            )
        )
        assertEquals("[1,2,3]", Snbt.toSnbt(tag))
    }
    
    @Test
    fun testCompoundTagToSnbt() {
        val tag = CompoundBinaryTag.builder()
            .putString("name", "Test")
            .putInt("age", 25)
            .putBoolean("active", true)
            .build()
        
        val snbt = Snbt.toSnbt(tag)
        
        // Compound tag order might vary, so check for presence
        assertTrue(snbt.startsWith("{"))
        assertTrue(snbt.endsWith("}"))
        assertTrue(snbt.contains("name:\"Test\""))
        assertTrue(snbt.contains("age:25"))
        assertTrue(snbt.contains("active:1b"))
    }
    
    @Test
    fun testCompoundTagWithSpecialCharactersInKey() {
        val tag = CompoundBinaryTag.builder()
            .putString("normal_key", "value1")
            .putString("key with spaces", "value2")
            .putString("key:with:colons", "value3")
            .build()
        
        val snbt = Snbt.toSnbt(tag)
        
        assertTrue(snbt.contains("normal_key:\"value1\""))
        assertTrue(snbt.contains("\"key with spaces\":\"value2\""))
        assertTrue(snbt.contains("\"key:with:colons\":\"value3\""))
    }
    
    @Test
    fun testNestedCompoundTags() {
        val innerTag = CompoundBinaryTag.builder()
            .putInt("x", 10)
            .putInt("y", 20)
            .build()
        
        val outerTag = CompoundBinaryTag.builder()
            .putString("type", "position")
            .put("coords", innerTag)
            .build()
        
        val snbt = Snbt.toSnbt(outerTag)
        
        assertTrue(snbt.contains("type:\"position\""))
        assertTrue(snbt.contains("coords:{"))
        assertTrue(snbt.contains("x:10"))
        assertTrue(snbt.contains("y:20"))
    }
    
    @Test
    fun testEmptyCompoundTag() {
        val tag = CompoundBinaryTag.empty()
        assertEquals("{}", Snbt.toSnbt(tag))
    }
    
    @Test
    fun testEmptyListTag() {
        val tag = ListBinaryTag.empty()
        assertEquals("[]", Snbt.toSnbt(tag))
    }
}
