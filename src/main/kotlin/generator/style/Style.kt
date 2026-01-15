package generator.style

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Style {
    @SerialName("medieval")
    MEDIEVAL,
    
    @SerialName("japanese")
    JAPANESE,
    
    @SerialName("desert")
    DESERT
}
