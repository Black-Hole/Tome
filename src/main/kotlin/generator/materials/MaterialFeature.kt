package generator.materials

import kotlinx.serialization.Serializable

enum class MaterialFeature {
    SHADE,
    WEAR,
    MOISTURE,
    DECORATION
}

val MATERIAL_FEATURE_TRAVERSAL_ORDER = listOf(
    MaterialFeature.DECORATION,
    MaterialFeature.SHADE,
    MaterialFeature.WEAR,
    MaterialFeature.MOISTURE
)

enum class MaterialFeatureMapping {
    LINEAR,
    FITTED
}

@Serializable
data class MaterialParameters(
    val shade: Float,
    val wear: Float,
    val moisture: Float,
    val decoration: Float
)

fun more(
    material: MaterialId,
    feature: MaterialFeature,
    materials: Map<MaterialId, Material>
): List<MaterialId> {
    val result = mutableListOf<MaterialId>()
    var currentMaterial = material

    while (true) {
        val materialData = materials[currentMaterial] ?: break
        val nextMaterial = materialData.more(feature) ?: break
        result.add(nextMaterial)
        currentMaterial = nextMaterial
    }

    return result
}

fun less(
    material: MaterialId,
    feature: MaterialFeature,
    materials: Map<MaterialId, Material>
): List<MaterialId> {
    val result = mutableListOf<MaterialId>()
    var currentMaterial = material

    while (true) {
        val materialData = materials[currentMaterial] ?: break
        val nextMaterial = materialData.less(feature) ?: break
        result.add(nextMaterial)
        currentMaterial = nextMaterial
    }

    return result
}

fun mapFeatures(
    parameters: MaterialParameters,
    material: MaterialId,
    materials: Map<MaterialId, Material>
): MaterialId {
    var currentMaterial = material

    for (feature in MATERIAL_FEATURE_TRAVERSAL_ORDER) {
        currentMaterial = mapFeature(
            value = when (feature) {
                MaterialFeature.SHADE -> parameters.shade
                MaterialFeature.WEAR -> parameters.wear
                MaterialFeature.MOISTURE -> parameters.moisture
                MaterialFeature.DECORATION -> parameters.decoration
            },
            material = currentMaterial,
            feature = feature,
            materials = materials,
            mapping = MaterialFeatureMapping.FITTED
        )
    }

    return currentMaterial
}

fun mapFeature(
    value: Float,
    material: MaterialId,
    feature: MaterialFeature,
    materials: Map<MaterialId, Material>,
    mapping: MaterialFeatureMapping
): MaterialId {
    val moreList = more(material, feature, materials).toMutableList()
    val lessList = less(material, feature, materials).toMutableList()

    return when (mapping) {
        MaterialFeatureMapping.LINEAR -> {
            val length = minOf(moreList.size, lessList.size)
            val materialsList = mutableListOf<MaterialId>()

            for (i in 0 until length) {
                materialsList.add(lessList[length - 1 - i])
            }

            materialsList.add(material)

            for (i in 0 until length) {
                materialsList.add(moreList[i])
            }

            val index = (value * (length * 2 + 1)).toInt()
            materialsList.getOrElse(index) { material }
        }
        MaterialFeatureMapping.FITTED -> {
            moreList.add(material)
            lessList.add(0, material)

            if (value < 0.5f) {
                val rescaledValue = 2.0f * (0.5f - value)
                val index = (rescaledValue * lessList.size).toInt()
                lessList[index.coerceIn(0, lessList.size - 1)]
            } else {
                val rescaledValue = 1.0f - 2.0f * (value - 0.5f)
                val index = (rescaledValue * moreList.size).toInt()
                moreList[index.coerceIn(0, moreList.size - 1)]
            }
        }
    }
}
