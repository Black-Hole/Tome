package http

import geometry.Rect3D
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import minecraft.Chunk
import minecraft.Chunks
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.zip.GZIPInputStream

private const val PROVIDER_LOG_LIMIT = 300
private const val MAX_RETRIES = 3

/**
 * HTTP provider for communicating with the GDMC HTTP interface.
 * 
 * This class provides methods for interacting with a Minecraft world through the GDMC HTTP mod,
 * including block placement, entity management, and world querying.
 */
class GDMCHTTPProvider(
    private val baseUrl: String = "http://localhost:9000",
    private val logResponses: Boolean = false
) {
    private val logger = LoggerFactory.getLogger(GDMCHTTPProvider::class.java)
    
    private val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    /**
     * Creates a new instance with response logging enabled.
     */
    fun withResponseLogging(enable: Boolean): GDMCHTTPProvider {
        return GDMCHTTPProvider(baseUrl, enable)
    }

    private fun url(path: String): String = "$baseUrl/$path"

    /**
     * Executes one or more Minecraft commands.
     * 
     * @param commands List of commands to execute
     * @return List of command responses
     */
    suspend fun command(commands: List<String>): Result<List<CommandResponse>> = withRetry {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url("command")))
            .header("Content-Type", "text/plain")
            .POST(HttpRequest.BodyPublishers.ofString(commands.joinToString("\n")))
            .timeout(Duration.ofSeconds(30))
            .build()
        
        val response = withContext(Dispatchers.IO) {
            client.send(request, HttpResponse.BodyHandlers.ofString())
        }
        
        val text = response.body()
        logResponse(text)
        json.decodeFromString<List<CommandResponse>>(text)
    }

    /**
     * Gives a written book to the player with specified pages, title, and author.
     * 
     * @param pages List of page contents
     * @param title Book title
     * @param author Book author
     * @return Command response
     */
    suspend fun givePlayerBook(pages: List<String>, title: String, author: String): Result<CommandResponse> = runCatching {
        val pagesJson = pages.joinToString(",")
        val cmd = "give @a written_book{pages:[$pagesJson],title:\"$title\",author:\"$author\"}"
        
        logger.info("Command: $cmd")
        
        command(listOf(cmd)).getOrThrow()[0]
    }

    /**
     * Retrieves blocks in a specified region.
     * 
     * @param x Starting x coordinate
     * @param y Starting y coordinate
     * @param z Starting z coordinate
     * @param dx Size in x direction
     * @param dy Size in y direction
     * @param dz Size in z direction
     * @return List of positioned blocks
     */
    suspend fun getBlocks(
        x: Int, y: Int, z: Int,
        dx: Int, dy: Int, dz: Int
    ): Result<List<PositionedBlock>> = withRetry {
        val includeState = true
        val includeData = true
        
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url("blocks?x=$x&y=$y&z=$z&dx=$dx&dy=$dy&dz=$dz&includeState=$includeState&includeData=$includeData")))
            .GET()
            .timeout(Duration.ofSeconds(30))
            .build()
        
        val response = withContext(Dispatchers.IO) {
            client.send(request, HttpResponse.BodyHandlers.ofString())
        }
        
        val text = response.body()
        logResponse(text)
        json.decodeFromString<List<PositionedBlock>>(text)
    }

    /**
     * Places blocks in the world.
     * 
     * @param blocks List of positioned blocks to place
     * @return List of block placement responses
     */
    suspend fun putBlocks(blocks: List<PositionedBlock>): Result<List<BlockPlacementResponse>> = withRetry {
        val body = json.encodeToString(blocks)
        
        logger.info("Sending PUT request to ${url("blocks")} with body: $body")
        
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url("blocks")))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(body))
            .timeout(Duration.ofSeconds(30))
            .build()
        
        val response = withContext(Dispatchers.IO) {
            client.send(request, HttpResponse.BodyHandlers.ofString())
        }
        
        val text = response.body()
        logResponse(text)
        json.decodeFromString<List<BlockPlacementResponse>>(text)
    }

    /**
     * Retrieves the current build area.
     * 
     * @return The build area as a Rect3D
     */
    suspend fun getBuildArea(): Result<Rect3D> = withRetry {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url("buildarea")))
            .GET()
            .timeout(Duration.ofSeconds(30))
            .build()
        
        val response = withContext(Dispatchers.IO) {
            client.send(request, HttpResponse.BodyHandlers.ofString())
        }
        
        val text = response.body()
        logResponse(text)
        val buildAreaResponse = json.decodeFromString<BuildAreaResponse>(text)
        buildAreaResponse.toRect()
    }

    /**
     * Retrieves a height map for a specified region.
     * 
     * @param x Starting x coordinate
     * @param z Starting z coordinate
     * @param dx Size in x direction
     * @param dz Size in z direction
     * @param heightMapType Type of height map
     * @return 2D array of heights
     */
    suspend fun getHeightmap(
        x: Int, z: Int,
        dx: Int, dz: Int,
        heightMapType: HeightMapType
    ): Result<List<List<Int>>> = withRetry {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url("heightmap?x=$x&z=$z&dx=$dx&dz=$dz&type=$heightMapType")))
            .GET()
            .timeout(Duration.ofSeconds(30))
            .build()
        
        val response = withContext(Dispatchers.IO) {
            client.send(request, HttpResponse.BodyHandlers.ofString())
        }
        
        val text = response.body()
        logResponse(text)
        json.decodeFromString<List<List<Int>>>(text)
    }

    /**
     * Retrieves biomes in a specified region.
     * 
     * @param x Starting x coordinate
     * @param y Starting y coordinate
     * @param z Starting z coordinate
     * @param dx Size in x direction
     * @param dy Size in y direction
     * @param dz Size in z direction
     * @return List of positioned biomes
     */
    suspend fun getBiomes(
        x: Int, y: Int, z: Int,
        dx: Int, dy: Int, dz: Int
    ): Result<List<PositionedBiome>> = withRetry {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url("biomes?x=$x&y=$y&z=$z&dx=$dx&dy=$dy&dz=$dz")))
            .GET()
            .timeout(Duration.ofSeconds(30))
            .build()
        
        val response = withContext(Dispatchers.IO) {
            client.send(request, HttpResponse.BodyHandlers.ofString())
        }
        
        val text = response.body()
        logResponse(text)
        json.decodeFromString<List<PositionedBiome>>(text)
    }

    /**
     * Retrieves chunks in a specified region.
     * 
     * @param x Starting x coordinate
     * @param y Starting y coordinate
     * @param z Starting z coordinate
     * @param dx Size in x direction
     * @param dy Size in y direction
     * @param dz Size in z direction
     * @return List of chunks
     */
    suspend fun getChunks(
        x: Int, y: Int, z: Int,
        dx: Int, dy: Int, dz: Int
    ): Result<List<Chunk>> = withRetry {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url("chunks?x=$x&y=$y&z=$z&dx=$dx&dy=$dy&dz=$dz")))
            .header("Accept-Encoding", "gzip")
            .GET()
            .timeout(Duration.ofSeconds(30))
            .build()
        
        val response = withContext(Dispatchers.IO) {
            client.send(request, HttpResponse.BodyHandlers.ofByteArray())
        }
        
        val rawBytes = response.body()
        val decompressedBytes = decompressGzip(rawBytes)
        logger.debug("Decompressed {} bytes from chunk data", decompressedBytes.size)
        
        // Try to parse as Chunks
        try {
            val chunks = parseNbt<Chunks>(decompressedBytes)
            logger.debug("Decompressed NBT value: $chunks")
            return@withRetry chunks.chunks
        } catch (e: Exception) {
            // Try double decompression
            val doubleDecompressed = decompressGzip(decompressedBytes)
            logger.debug("Decompressed {} bytes from NBT data", doubleDecompressed.size)
            
            val chunks = parseNbt<Chunks>(doubleDecompressed)
            logger.debug("Decompressed NBT value: $chunks")
            chunks.chunks
        }
    }

    /**
     * Retrieves entities in a specified region.
     * 
     * @param x Starting x coordinate
     * @param y Starting y coordinate
     * @param z Starting z coordinate
     * @param dx Size in x direction
     * @param dy Size in y direction
     * @param dz Size in z direction
     * @return List of entity responses
     */
    suspend fun getEntities(
        x: Int, y: Int, z: Int,
        dx: Int, dy: Int, dz: Int
    ): Result<List<EntityResponse>> = withRetry {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url("entities?x=$x&y=$y&z=$z&dx=$dx&dy=$dy&dz=$dz")))
            .GET()
            .timeout(Duration.ofSeconds(30))
            .build()
        
        val response = withContext(Dispatchers.IO) {
            client.send(request, HttpResponse.BodyHandlers.ofString())
        }
        
        val text = response.body()
        logResponse(text)
        json.decodeFromString<List<EntityResponse>>(text)
    }

    /**
     * Places entities in the world.
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param entities List of positioned entities to place
     */
    suspend fun putEntities(
        x: Int, y: Int, z: Int,
        entities: List<PositionedEntity>
    ): Result<Unit> = withRetry {
        val body = json.encodeToString(entities)
        
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url("entities?x=$x&y=$y&z=$z")))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(body))
            .timeout(Duration.ofSeconds(30))
            .build()
        
        val response = withContext(Dispatchers.IO) {
            client.send(request, HttpResponse.BodyHandlers.ofString())
        }
        
        val text = response.body()
        logResponse(text)
    }

    /**
     * Executes a block with retry logic.
     */
    private suspend fun <T> withRetry(block: suspend () -> T): Result<T> {
        var lastException: Exception? = null
        
        repeat(MAX_RETRIES) { attempt ->
            try {
                return Result.success(block())
            } catch (e: IOException) {
                lastException = e
                if (attempt < MAX_RETRIES - 1) {
                    val delayMs = (1L shl attempt) * 1000 // Exponential backoff: 1s, 2s, 4s
                    logger.debug("Request failed (attempt ${attempt + 1}/$MAX_RETRIES), retrying in ${delayMs}ms: ${e.message}")
                    delay(delayMs)
                }
            } catch (e: Exception) {
                return Result.failure(e)
            }
        }
        
        return Result.failure(lastException ?: IOException("Request failed after $MAX_RETRIES attempts"))
    }

    private fun logResponse(text: String) {
        if (!logResponses) return
        
        if (text.length > PROVIDER_LOG_LIMIT) {
            logger.info("Response: ${text.take(PROVIDER_LOG_LIMIT)}...")
        } else {
            logger.info("Response: $text")
        }
    }

    /**
     * Decompresses GZIP-compressed data.
     */
    private fun decompressGzip(data: ByteArray): ByteArray {
        return GZIPInputStream(ByteArrayInputStream(data)).use { it.readBytes() }
    }

    /**
     * Parses NBT data (placeholder for actual NBT parsing).
     * Note: This would need to use fastnbt or similar NBT parsing library.
     */
    private inline fun <reified T> parseNbt(data: ByteArray): T {
        // TODO: Implement actual NBT parsing using a Kotlin NBT library
        // For now, we'll use JSON as a fallback since Chunks is serializable
        throw UnsupportedOperationException("NBT parsing not yet implemented in Kotlin version")
    }
}
