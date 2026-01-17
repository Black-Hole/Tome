package minecraft

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Chunks(
    @SerialName("Chunks")
    val chunks: List<Chunk>
)

@Serializable
data class Chunk(
    @SerialName("xPos")
    val xPos: Int,
    @SerialName("yPos")
    val yPos: Int,
    @SerialName("zPos")
    val zPos: Int,
    @SerialName("sections")
    val sections: List<ChunkSection>,
    @SerialName("Heightmaps")
    val heightmaps: HeightMaps,
    @SerialName("block_entities")
    val blockEntities: List<JsonElement>? = null
)

@Serializable
data class ChunkSection(
    @SerialName("Y")
    val y: Int,
    @SerialName("block_states")
    val blockStates: BlockStates? = null,
    @SerialName("biomes")
    val biomes: BiomesData? = null
)

@Serializable
data class BlockStates(
    @SerialName("palette")
    val palette: List<ChunkBlock>,
    @SerialName("data")
    val data: LongArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BlockStates) return false
        if (palette != other.palette) return false
        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false
        return true
    }

    override fun hashCode(): Int {
        var result = palette.hashCode()
        result = 31 * result + (data?.contentHashCode() ?: 0)
        return result
    }
}

@Serializable
data class ChunkBlock(
    @SerialName("Name")
    val name: String,
    @SerialName("Properties")
    val properties: Map<String, String>? = null
)

@Serializable
data class BiomesData(
    @SerialName("palette")
    val biomes: List<Biome>,
    @SerialName("data")
    val data: LongArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BiomesData) return false
        if (biomes != other.biomes) return false
        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false
        return true
    }

    override fun hashCode(): Int {
        var result = biomes.hashCode()
        result = 31 * result + (data?.contentHashCode() ?: 0)
        return result
    }
}

@Serializable
data class HeightMaps(
    @SerialName("MOTION_BLOCKING")
    val motionBlocking: LongArray,
    @SerialName("MOTION_BLOCKING_NO_LEAVES")
    val motionBlockingNoLeaves: LongArray,
    @SerialName("OCEAN_FLOOR")
    val oceanFloor: LongArray,
    @SerialName("OCEAN_FLOOR_WG")
    val oceanFloorWg: LongArray? = null,
    @SerialName("WORLD_SURFACE")
    val worldSurface: LongArray,
    @SerialName("WORLD_SURFACE_WG")
    val worldSurfaceWg: LongArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HeightMaps) return false
        if (!motionBlocking.contentEquals(other.motionBlocking)) return false
        if (!motionBlockingNoLeaves.contentEquals(other.motionBlockingNoLeaves)) return false
        if (!oceanFloor.contentEquals(other.oceanFloor)) return false
        if (oceanFloorWg != null) {
            if (other.oceanFloorWg == null) return false
            if (!oceanFloorWg.contentEquals(other.oceanFloorWg)) return false
        } else if (other.oceanFloorWg != null) return false
        if (!worldSurface.contentEquals(other.worldSurface)) return false
        if (worldSurfaceWg != null) {
            if (other.worldSurfaceWg == null) return false
            if (!worldSurfaceWg.contentEquals(other.worldSurfaceWg)) return false
        } else if (other.worldSurfaceWg != null) return false
        return true
    }

    override fun hashCode(): Int {
        var result = motionBlocking.contentHashCode()
        result = 31 * result + motionBlockingNoLeaves.contentHashCode()
        result = 31 * result + oceanFloor.contentHashCode()
        result = 31 * result + (oceanFloorWg?.contentHashCode() ?: 0)
        result = 31 * result + worldSurface.contentHashCode()
        result = 31 * result + (worldSurfaceWg?.contentHashCode() ?: 0)
        return result
    }
}
