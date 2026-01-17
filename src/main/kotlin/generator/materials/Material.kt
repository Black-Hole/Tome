package generator.materials

import data.Loadable
import editor.Editor
import geometry.Point3D
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import minecraft.Block
import minecraft.BlockForm
import minecraft.BlockID
import noise.RNG
import org.slf4j.LoggerFactory

@Serializable
data class MaterialId(val value: String) {
    companion object {
        fun new(id: String): MaterialId = MaterialId(id)
    }

    fun asStr(): String = value
}

@Serializable
data class Material(
    val id: MaterialId,
    val connections: MaterialConnections? = null,
    val blocks: Map<BlockForm, MaterialBlocks>
) {
    fun id(): MaterialId = id

    fun more(feature: MaterialFeature): MaterialId? {
        return connections?.let { conn ->
            when (feature) {
                MaterialFeature.SHADE -> conn.lighter
                MaterialFeature.WEAR -> conn.moreWorn
                MaterialFeature.MOISTURE -> conn.wetter
                MaterialFeature.DECORATION -> conn.moreDecorated
            }
        }
    }

    fun less(feature: MaterialFeature): MaterialId? {
        return connections?.let { conn ->
            when (feature) {
                MaterialFeature.SHADE -> conn.darker
                MaterialFeature.WEAR -> conn.lessWorn
                MaterialFeature.MOISTURE -> conn.drier
                MaterialFeature.DECORATION -> conn.lessDecorated
            }
        }
    }

    fun getBlock(form: BlockForm, rng: RNG): BlockID? {
        return when (val materialBlocks = blocks[form]) {
            is MaterialBlocks.Block -> materialBlocks.block
            is MaterialBlocks.Blocks -> rng.chooseWeighted(materialBlocks.blocks)
            null -> null
        }
    }

    suspend fun placeBlock(
        editor: Editor,
        point: Point3D,
        form: BlockForm,
        materials: Map<MaterialId, Material>,
        state: Map<String, String>?,
        data: String?,
        parameters: MaterialParameters,
        rng: RNG,
        isForced: Boolean
    ) {
        val materialId = mapFeatures(parameters, id, materials)

        val blockId = materials[materialId]?.getBlock(form, rng)
        if (blockId != null) {
            editor.placeBlockOptions(
                Block(
                    id = blockId,
                    state = state,
                    data = data
                ),
                point,
                isForced
            )
        } else {
            logger.warn("No block found for material {} with form {}", id.value, form)
        }
    }

    fun getForm(id: BlockID): BlockForm? {
        for ((form, materialBlocks) in blocks) {
            when (materialBlocks) {
                is MaterialBlocks.Block -> {
                    if (materialBlocks.block == id) {
                        return form
                    }
                }
                is MaterialBlocks.Blocks -> {
                    if (materialBlocks.blocks.containsKey(id)) {
                        return form
                    }
                }
            }
        }
        return null
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Material::class.java)
    }
}

@Serializable
data class MaterialConnections(
    // Shade
    val lighter: MaterialId? = null,
    val darker: MaterialId? = null,
    // Wear
    @SerialName("more_worn")
    val moreWorn: MaterialId? = null,
    @SerialName("less_worn")
    val lessWorn: MaterialId? = null,
    // Moisture
    val wetter: MaterialId? = null,
    val drier: MaterialId? = null,
    // Decoration
    @SerialName("more_decorated")
    val moreDecorated: MaterialId? = null,
    @SerialName("less_decorated")
    val lessDecorated: MaterialId? = null
)

@Serializable(with = MaterialBlocksSerializer::class)
sealed class MaterialBlocks {
    @Serializable
    data class Block(val block: BlockID) : MaterialBlocks()

    @Serializable
    data class Blocks(val blocks: Map<BlockID, Float>) : MaterialBlocks()
}

object MaterialLoader : Loadable<Material, MaterialId> {
    override fun getKey(item: Material): MaterialId = item.id

    override fun path(): String = "materials"

    override fun deserializer() = Material.serializer()

    override fun postLoad(items: MutableMap<MaterialId, Material>) {
        // No post-processing needed
    }
}
