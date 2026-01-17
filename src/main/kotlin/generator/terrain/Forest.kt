package generator.terrain

import data.Loadable
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.DeserializationStrategy
import org.slf4j.LoggerFactory

@Serializable
data class ForestId(val id: String) {
    fun asStr(): String = id
    
    companion object {
        fun new(id: String): ForestId = ForestId(id)
    }
}

@Serializable
data class Forest(
    val id: ForestId,
    val trees: Map<Tree, Float>,
    @SerialName("tree_palette")
    val treePalette: Map<Tree, Map<String, Map<String, Float>>>,
    @SerialName("tree_density")
    val treeDensity: Int
) {
    fun id(): ForestId = id
    fun trees(): Map<Tree, Float> = trees
    fun treePalette(): Map<Tree, Map<String, Map<String, Float>>> = treePalette
    fun treeDensity(): Int = treeDensity
    
    companion object : Loadable<Forest, ForestId> {
        private val logger = LoggerFactory.getLogger(Forest::class.java)
        
        override fun getKey(item: Forest): ForestId = item.id
        
        override fun path(): String = "forests"
        
        override fun deserializer(): DeserializationStrategy<Forest> = serializer()
        
        override fun postLoad(items: MutableMap<ForestId, Forest>) {
            // No post-processing needed
        }
    }
}
