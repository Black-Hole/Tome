package minecraft

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
enum class Color {
    @SerialName("black")
    BLACK,
    @SerialName("gray")
    GRAY,
    @SerialName("light_gray")
    LIGHT_GRAY,
    @SerialName("blue")
    BLUE,
    @SerialName("cyan")
    CYAN,
    @SerialName("green")
    GREEN,
    @SerialName("red")
    RED,
    @SerialName("purple")
    PURPLE,
    @SerialName("yellow")
    YELLOW,
    @SerialName("white")
    WHITE,
    @SerialName("orange")
    ORANGE,
    @SerialName("magenta")
    MAGENTA,
    @SerialName("light_blue")
    LIGHT_BLUE,
    @SerialName("lime")
    LIME,
    @SerialName("pink")
    PINK,
    @SerialName("brown")
    BROWN;

    fun toColorString(): String = when (this) {
        BLACK -> "black"
        GRAY -> "gray"
        LIGHT_GRAY -> "light_gray"
        BLUE -> "blue"
        CYAN -> "cyan"
        GREEN -> "green"
        RED -> "red"
        PURPLE -> "purple"
        YELLOW -> "yellow"
        WHITE -> "white"
        ORANGE -> "orange"
        MAGENTA -> "magenta"
        LIGHT_BLUE -> "light_blue"
        LIME -> "lime"
        PINK -> "pink"
        BROWN -> "brown"
    }
}

private val SWAPPABLE_STRINGS = listOf(
    "wool",
    "carpet",
    "stained_glass",
    "terracotta",
    "concrete",
    "shulker_box",
    "bed",
    "candle",
    "banner"
)

/**
 * Recolors a block if it matches the old color, replacing it with the new color.
 */
fun recolorBlock(blockId: BlockID, oldColor: Color, newColor: Color): BlockID {
    val blockIdStr = Json.encodeToString(blockId)
    
    if (!SWAPPABLE_STRINGS.any { blockIdStr.contains(it) }) {
        return blockId
    }
    
    val oldColorStr = oldColor.toColorString()
    val newColorStr = newColor.toColorString()
    
    // Don't replace light colors with dark colors
    if (oldColorStr == "blue" && blockIdStr.contains("light_blue")) {
        return blockId
    }
    if (oldColorStr == "gray" && blockIdStr.contains("light_gray")) {
        return blockId
    }
    
    if (blockIdStr.contains(oldColorStr)) {
        return Json.decodeFromString(blockIdStr.replace(oldColorStr, newColorStr))
    }
    
    return blockId
}

/**
 * Colors a block by detecting its current color and replacing it with the specified color.
 */
fun colorBlock(blockId: BlockID, color: Color): BlockID {
    val blockIdStr = Json.encodeToString(blockId)
    
    if (!SWAPPABLE_STRINGS.any { blockIdStr.contains(it) }) {
        return blockId
    }
    
    for (currentColor in Color.entries) {
        val colorIn = Json.encodeToString(currentColor)
        val colorOut = color.toColorString()
        if (blockIdStr.contains(colorIn)) {
            return Json.decodeFromString(blockIdStr.replace(colorIn, colorOut))
        }
    }
    
    return blockId
}
