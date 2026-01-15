package generator.materials

import data.Loadable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import minecraft.BlockForm
import minecraft.BlockID
import minecraft.Color
import minecraft.recolorBlock
import noise.RNG

@Serializable
data class PaletteId(val value: String) {
    companion object {
        fun from(id: String): PaletteId = PaletteId(id)
    }

    fun intoString(): String = value
}

@Serializable
data class Palette(
    val id: PaletteId,
    @SerialName("primary_color")
    val primaryColor: Color? = null,
    @SerialName("secondary_color")
    val secondaryColor: Color? = null,
    val tags: List<String>? = null,
    val accent: MaterialId? = null,
    @SerialName("primary_wall")
    val primaryWall: MaterialId? = null,
    @SerialName("secondary_wall")
    val secondaryWall: MaterialId? = null,
    @SerialName("primary_roof")
    val primaryRoof: MaterialId? = null,
    @SerialName("secondary_roof")
    val secondaryRoof: MaterialId? = null,
    @SerialName("wood_pillar")
    val woodPillar: MaterialId? = null,
    @SerialName("stone_pillar")
    val stonePillar: MaterialId? = null,
    @SerialName("primary_stone")
    val primaryStone: MaterialId? = null,
    @SerialName("secondary_stone")
    val secondaryStone: MaterialId? = null,
    @SerialName("primary_wood")
    val primaryWood: MaterialId? = null,
    @SerialName("secondary_wood")
    val secondaryWood: MaterialId? = null,
    val flower: MaterialId? = null
) {
    // Helper to get materials map
    private val materialsMap: Map<MaterialRole, MaterialId>
        get() = buildMap {
            accent?.let { put(MaterialRole.ACCENT, it) }
            primaryWall?.let { put(MaterialRole.PRIMARY_WALL, it) }
            secondaryWall?.let { put(MaterialRole.SECONDARY_WALL, it) }
            primaryRoof?.let { put(MaterialRole.PRIMARY_ROOF, it) }
            secondaryRoof?.let { put(MaterialRole.SECONDARY_ROOF, it) }
            woodPillar?.let { put(MaterialRole.WOOD_PILLAR, it) }
            stonePillar?.let { put(MaterialRole.STONE_PILLAR, it) }
            primaryStone?.let { put(MaterialRole.PRIMARY_STONE, it) }
            secondaryStone?.let { put(MaterialRole.SECONDARY_STONE, it) }
            primaryWood?.let { put(MaterialRole.PRIMARY_WOOD, it) }
            secondaryWood?.let { put(MaterialRole.SECONDARY_WOOD, it) }
            flower?.let { put(MaterialRole.FLOWER, it) }
        }

    fun getMaterial(role: MaterialRole): MaterialId? {
        var currentRole = role
        var iterations = 0

        while (!materialsMap.containsKey(currentRole)) {
            val newRole = currentRole.backupRole()

            if (newRole == currentRole || iterations >= 5) {
                return null
            }

            currentRole = newRole
            iterations++
        }

        return materialsMap[currentRole]
            ?: error("Material role $currentRole not found in palette $id")
    }

    fun getBlock(
        role: MaterialRole,
        form: BlockForm,
        materials: Map<MaterialId, Material>,
        rng: RNG
    ): BlockID? {
        val materialId = getMaterial(role) ?: return null
        return materials[materialId]?.getBlock(form, rng)
    }

    fun findRoleAndForm(
        block: BlockID,
        materials: Map<MaterialId, Material>
    ): Pair<MaterialRole, BlockForm>? {
        for (role in listOf(
            MaterialRole.FLOWER,
            MaterialRole.ACCENT,
            MaterialRole.PRIMARY_WALL,
            MaterialRole.SECONDARY_WALL,
            MaterialRole.PRIMARY_ROOF,
            MaterialRole.SECONDARY_ROOF,
            MaterialRole.WOOD_PILLAR,
            MaterialRole.STONE_PILLAR,
            MaterialRole.SECONDARY_STONE,
            MaterialRole.SECONDARY_WOOD,
            MaterialRole.PRIMARY_STONE,
            MaterialRole.PRIMARY_WOOD
        )) {
            val materialId = getMaterial(role) ?: continue
            val material = materials[materialId] ?: error("Material $materialId not found")

            material.getForm(block)?.let { form ->
                return Pair(role, form)
            }
        }

        return null
    }

    fun swapWith(
        block: BlockID,
        outputPalette: Palette,
        materials: Map<MaterialId, Material>
    ): PaletteSwapResult {
        findRoleAndForm(block, materials)?.let { (role, form) ->
            outputPalette.getMaterial(role)?.let { materialId ->
                return PaletteSwapResult.Material(materialId, form)
            }
        }

        return PaletteSwapResult.Block(recolorBlock(block, outputPalette))
    }

    fun recolorBlock(block: BlockID, outputPalette: Palette): BlockID {
        var recolored = block

        if (primaryColor != null && outputPalette.primaryColor != null) {
            recolored = recolorBlock(recolored, primaryColor, outputPalette.primaryColor)
        }

        if (secondaryColor != null && outputPalette.secondaryColor != null) {
            recolored = recolorBlock(recolored, secondaryColor, outputPalette.secondaryColor)
        }

        return recolored
    }
}

sealed class PaletteSwapResult {
    data class Block(val blockId: BlockID) : PaletteSwapResult()
    data class Material(val materialId: MaterialId, val form: BlockForm) : PaletteSwapResult()
}

object PaletteLoader : Loadable<Palette, PaletteId> {
    override fun getKey(item: Palette): PaletteId = item.id

    override fun path(): String = "palettes"

    override fun deserializer() = Palette.serializer()

    override fun postLoad(items: MutableMap<PaletteId, Palette>) {
        // No post-processing needed
    }
}
