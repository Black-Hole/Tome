package data

import config.Config
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isDirectory

/**
 * Interface for types that can be loaded from JSON files in the data directory.
 *
 * Implementations of this interface can load data from JSON files recursively
 * from a specified subdirectory in the data/ folder.
 *
 * @param TItem The type of items being loaded (must be deserializable)
 * @param TKey The type of key used to identify items in the map
 */
interface Loadable<TItem, TKey> {
    
    /**
     * Load all items from the data directory.
     *
     * Recursively scans the subdirectory defined by [path] for JSON files,
     * deserializes them, and returns a map of items keyed by [getKey].
     *
     * @return A map of loaded items
     * @throws Exception if loading fails
     */
    fun load(): Map<TKey, TItem> {
        val dataPath = File(System.getProperty("user.dir"))
            .resolve(Config.DATA_PATH)
            .resolve(path())
            .toPath()
        
        logger.info("Loading items from {}", dataPath)
        
        val items = mutableMapOf<TKey, TItem>()
        loadAllIn(dataPath, items)
        postLoad(items)
        return items
    }
    
    /**
     * Recursively load all JSON files from the given path.
     *
     * @param path The directory path to scan
     * @param items The mutable map to populate with loaded items
     */
    private fun loadAllIn(path: Path, items: MutableMap<TKey, TItem>) {
        if (!Files.exists(path)) {
            logger.warn("Path does not exist: {}", path)
            return
        }
        
        Files.list(path).use { stream ->
            stream.forEach { entryPath ->
                when {
                    entryPath.isDirectory() -> {
                        loadAllIn(entryPath, items)
                    }
                    entryPath.extension == "json" -> {
                        try {
                            val content = Files.readString(entryPath)
                            val item = json.decodeFromString(deserializer(), content)
                            val key = getKey(item)
                            items[key] = item
                        } catch (e: Exception) {
                            logger.warn("Failed to deserialize {}: {}", entryPath, e.message)
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Extract the key from an item.
     *
     * @param item The item to extract the key from
     * @return The key for this item
     */
    fun getKey(item: TItem): TKey
    
    /**
     * Post-process loaded items after all items have been loaded.
     *
     * This can be used to perform additional setup or validation
     * after the initial loading phase.
     *
     * @param items The mutable map of loaded items
     */
    fun postLoad(items: MutableMap<TKey, TItem>) {
        // Default implementation does nothing
    }
    
    /**
     * Get the subdirectory path within the data/ folder.
     *
     * @return The relative path (e.g., "palettes", "buildings", etc.)
     */
    fun path(): String
    
    /**
     * Get the deserializer for the item type.
     *
     * @return The deserialization strategy for TItem
     */
    fun deserializer(): DeserializationStrategy<TItem>
    
    companion object {
        private val logger = LoggerFactory.getLogger(Loadable::class.java)
        
        /**
         * JSON configuration for deserialization.
         */
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }
    }
}
