package generator.districts

import editor.Editor
import geometry.CARDINALS_2D
import geometry.Point2D
import minecraft.Biome
import minecraft.BlockID
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

data class DistrictAnalysis(
    val count: Int,
    val roughness: Float,
    val waterPercentage: Float,
    val forestedPercentage: Float,
    val surfaceBlockCount: Map<BlockID, Int>,
    val biomeCount: Map<Biome, Int>,
    val gradient: Float
) {
    fun biomePercentage(biome: Biome): Float {
        val countVal = biomeCount[biome] ?: return 0.0f
        return (countVal * 100).toFloat() / count.toFloat()
    }
}

suspend fun <TID> analyzeDistrict(area: DistrictData<TID>, editor: Editor): DistrictAnalysis {
    val average = area.average()
    val averageHeight = average.y
    val numberOfPoints = area.points.size.toFloat()

    var waterBlocks = 0
    var leafBlocks = 0
    var neighbourHeightSum = 0.0f
    var rootMeanSquareHeight = 0.0f

    val biomeCount = mutableMapOf<Biome, Int>()
    val surfaceBlockCount = mutableMapOf<BlockID, Int>()

    for (point in area.points) {
        val biome = editor.world().getSurfaceBiomeAt(point.dropY())
        val block = editor.getBlock(point)
        val isWater = block.id.isWater()
        val leafHeight = editor.world().getMotionBlockingHeightAt(point.dropY())

        rootMeanSquareHeight += (point.y - averageHeight).toFloat().pow(2)

        val height = editor.world().getHeightAt(point.dropY())
        val averageNeighbourHeight = CARDINALS_2D.sumOf { cardinal ->
            val neighbour = point.dropY() + cardinal
            if (editor.world().isInBounds2d(neighbour)) {
                abs(height - editor.world().getHeightAt(neighbour))
            } else {
                0
            }
        }.toFloat() / 4.0f

        neighbourHeightSum += averageNeighbourHeight

        biomeCount[biome] = (biomeCount[biome] ?: 0) + 1
        surfaceBlockCount[block.id] = (surfaceBlockCount[block.id] ?: 0) + 1

        if (isWater) {
            waterBlocks++
        }
        if (point.y < leafHeight) {
            leafBlocks++
        }
    }

    val numPoints = if (numberOfPoints == 0.0f) 1.0f else numberOfPoints
    return DistrictAnalysis(
        count = area.points.size,
        roughness = sqrt(rootMeanSquareHeight / numPoints),
        gradient = neighbourHeightSum / numPoints,
        waterPercentage = (waterBlocks.toFloat() / numPoints) * 100.0f,
        forestedPercentage = (leafBlocks.toFloat() / numPoints) * 100.0f,
        surfaceBlockCount = surfaceBlockCount,
        biomeCount = biomeCount
    )
}
