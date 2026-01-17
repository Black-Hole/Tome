package generator.materials

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MaterialRole {
    @SerialName("accent")
    ACCENT,

    @SerialName("primary_wall")
    PRIMARY_WALL,
    @SerialName("secondary_wall")
    SECONDARY_WALL,

    @SerialName("primary_roof")
    PRIMARY_ROOF,
    @SerialName("secondary_roof")
    SECONDARY_ROOF,

    @SerialName("wood_pillar")
    WOOD_PILLAR,
    @SerialName("stone_pillar")
    STONE_PILLAR,

    @SerialName("primary_stone")
    PRIMARY_STONE,
    @SerialName("secondary_stone")
    SECONDARY_STONE,
    @SerialName("primary_wood")
    PRIMARY_WOOD,
    @SerialName("secondary_wood")
    SECONDARY_WOOD,

    @SerialName("flower")
    FLOWER;

    fun backupRole(): MaterialRole = when (this) {
        SECONDARY_STONE -> PRIMARY_STONE
        SECONDARY_WOOD -> PRIMARY_WOOD
        ACCENT -> PRIMARY_STONE
        PRIMARY_WALL -> PRIMARY_STONE
        SECONDARY_WALL -> SECONDARY_STONE
        WOOD_PILLAR -> PRIMARY_WOOD
        STONE_PILLAR -> PRIMARY_STONE
        PRIMARY_ROOF -> PRIMARY_STONE
        SECONDARY_ROOF -> SECONDARY_STONE
        else -> this // PRIMARY_STONE and others remain unchanged
    }
}
