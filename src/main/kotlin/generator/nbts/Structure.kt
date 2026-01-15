package generator.nbts

import data.Loadable
import data.Point3DSerializer
import generator.materials.PaletteId
import generator.style.Style
import geometry.Cardinal
import geometry.Point3D
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
data class StructureId(val value: String) {
    companion object {
        fun from(id: String): StructureId = StructureId(id)
    }

    override fun toString(): String = value
}

@Serializable
data class Structure(
    val id: StructureId,
    @SerialName("path")
    val meta: NBTMeta,
    val facing: Cardinal = Cardinal.NORTH,
    @Serializable(with = Point3DSerializer::class)
    val origin: Point3D = Point3D(0, 0, 0),
    val palette: PaletteId? = null,
    val tags: List<String>? = null,
    @SerialName("mirror_x")
    val mirrorX: Boolean = false,
    @SerialName("mirror_z")
    val mirrorZ: Boolean = false,
    val style: Style? = null,
    val weight: Float = 1.0f
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Structure) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object : Loadable<Structure, StructureId> {
        override fun getKey(item: Structure): StructureId = item.id

        override fun postLoad(items: MutableMap<StructureId, Structure>) {
            // Normalize paths
            items.values.forEach { structure ->
                structure.meta.path = structure.meta.path.replace('\\', '/')
            }
        }

        override fun path(): String = "structures"

        override fun deserializer() = serializer()
    }
}
