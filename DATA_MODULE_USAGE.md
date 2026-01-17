# Data Module Usage

This document provides examples of how to use the translated data module in Kotlin.

## Loadable Interface

The `Loadable` interface provides a generic way to load JSON data from the `data/` directory.

### Basic Usage

```kotlin
import data.Loadable
import kotlinx.serialization.Serializable
import kotlinx.serialization.DeserializationStrategy

// Define your data model
@Serializable
data class Palette(
    val id: String,
    val primary_wall: String,
    val primary_wood: String,
    val tags: List<String> = emptyList()
)

// Create a loader implementation
class PaletteLoader : Loadable<Palette, String> {
    override fun getKey(item: Palette): String = item.id
    
    override fun path(): String = "palettes"
    
    override fun deserializer(): DeserializationStrategy<Palette> = 
        Palette.serializer()
    
    // Optional: Post-process loaded items
    override fun postLoad(items: MutableMap<String, Palette>) {
        println("Loaded ${items.size} palettes")
    }
}

// Load all palettes
fun main() {
    val loader = PaletteLoader()
    val palettes = loader.load()
    
    // Access loaded data
    palettes.forEach { (id, palette) ->
        println("Palette: $id - ${palette.primary_wall}")
    }
}
```

### Key Features

- **Recursive Loading**: Automatically scans subdirectories
- **Error Handling**: Malformed JSON files are logged and skipped
- **Type Safe**: Uses kotlinx.serialization for type-safe deserialization
- **Flexible**: Customize key extraction and post-processing

## SNBT Utilities

The `Snbt` object provides utilities to convert Adventure NBT binary tags to SNBT (Stringified NBT) format.

### Basic Usage

```kotlin
import data.Snbt
import net.kyori.adventure.nbt.*

fun main() {
    // Create a compound NBT tag
    val tag = CompoundBinaryTag.builder()
        .putString("Name", "Diamond Sword")
        .putInt("Damage", 10)
        .putBoolean("Unbreakable", true)
        .build()
    
    // Convert to SNBT
    val snbt = Snbt.toSnbt(tag)
    println(snbt)
    // Output: {Name:"Diamond Sword",Damage:10,Unbreakable:1b}
}
```

### Supported Tag Types

- **Primitive Types**: Byte, Short, Int, Long, Float, Double, String
- **Array Types**: ByteArray, IntArray, LongArray
- **Collection Types**: List, Compound (nested structures)

### Examples

```kotlin
// Byte tag
val byteTag = ByteBinaryTag.byteBinaryTag(42)
Snbt.toSnbt(byteTag) // "42b"

// Int array
val intArray = IntArrayBinaryTag.intArrayBinaryTag(1, 2, 3)
Snbt.toSnbt(intArray) // "[I;1,2,3]"

// Nested compound
val nested = CompoundBinaryTag.builder()
    .putString("type", "position")
    .put("coords", CompoundBinaryTag.builder()
        .putInt("x", 10)
        .putInt("y", 64)
        .putInt("z", -5)
        .build())
    .build()
Snbt.toSnbt(nested) // {type:"position",coords:{x:10,y:64,z:-5}}
```

## Migration from Rust

If you're migrating from the Rust version:

### Loadable Trait → Interface

| Rust | Kotlin |
|------|--------|
| `impl Loadable for MyType` | `class MyLoader : Loadable<MyType, KeyType>` |
| `fn get_key(item: &TItem) -> TKey` | `fun getKey(item: TItem): TKey` |
| `fn path() -> &'static str` | `fun path(): String` |
| `fn post_load(items: &mut HashMap)` | `fun postLoad(items: MutableMap)` |

### SNBT Function

| Rust | Kotlin |
|------|--------|
| `to_snbt(&value)` | `Snbt.toSnbt(tag)` |
| `fastnbt::Value` | `net.kyori.adventure.nbt.BinaryTag` |

## Best Practices

1. **Use `@Serializable`** on data classes for automatic serialization
2. **Implement `postLoad`** for cross-referencing or validation
3. **Handle errors gracefully** - malformed JSON is automatically skipped
4. **Use Adventure NBT** for all NBT operations
5. **Keep data files in `data/` directory** at project root
