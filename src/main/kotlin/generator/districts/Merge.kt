package generator.districts

import editor.Editor
import editor.World
import org.slf4j.LoggerFactory
import kotlin.math.abs

private val logger = LoggerFactory.getLogger("Merge")

suspend fun mergeDown(
    superdistricts: MutableMap<SuperDistrictID, SuperDistrict>,
    districts: Map<DistrictID, District>,
    districtAnalysisData: MutableMap<SuperDistrictID, DistrictAnalysis>,
    editor: Editor
) {
    var districtCount = superdistricts.size
    val ignore = mutableSetOf<SuperDistrictID>()

    while (districtCount > TARGET_DISTRICT_AMOUNT) {
        val child = superdistricts.entries
            .filter { (id, _) -> id !in ignore }
            .minByOrNull { (_, district) -> district.size() }
            ?.key

        if (child == null) {
            logger.info("Out of districts to merge, stopping.")
            break
        }

        val neighbours = superdistricts[child]
            ?.districtAdjacency()
            ?.keys
            ?.toList()
            ?: emptyList()

        logger.info("Options for child ${child.value} are $neighbours")
        val parent = getBestMergeCandidate(superdistricts, districtAnalysisData, child, neighbours)

        if (parent == null) {
            ignore.add(child)
            logger.info("No suitable parent found for child ${child.value}, ignoring it.")

            if ((superdistricts[child]?.size() ?: 0) < 10) {
                removeDistrict(superdistricts, child, editor.world())
                districtCount--
            }

            continue
        }

        merge(superdistricts, districts, districtAnalysisData, parent, child, editor)
        districtCount--
    }
}

private fun removeDistrict(
    districts: MutableMap<SuperDistrictID, SuperDistrict>,
    districtId: SuperDistrictID,
    world: World
) {
    val district = districts[districtId]
        ?: error("Superdistrict with id ${districtId.value} not found")

    for (point in district.points2d()) {
        world.superDistrictMap[point.x][point.y] = null
    }

    districts.remove(districtId)
}

private suspend fun merge(
    superdistricts: MutableMap<SuperDistrictID, SuperDistrict>,
    districts: Map<DistrictID, District>,
    districtAnalysisData: MutableMap<SuperDistrictID, DistrictAnalysis>,
    parent: SuperDistrictID,
    child: SuperDistrictID,
    editor: Editor
) {
    val childDistrict = superdistricts.remove(child)
        ?: error("Superdistrict with id ${child.value} not found")

    val districtIds = superdistricts.keys.filter { it != parent }
    for (id in districtIds) {
        val district = superdistricts[id]
            ?: error("Superdistrict with id ${id.value} not found")
        val amount = district.data.districtAdjacency.remove(childDistrict.id) ?: 0
        if (amount > 0) {
            district.data.districtAdjacency[parent] =
                (district.data.districtAdjacency[parent] ?: 0) + amount
        }
    }

    val parentDistrict = superdistricts[parent]
        ?: error("Superdistrict with id ${parent.value} not found")
    parentDistrict.addSuperdistrict(childDistrict, districts, editor.world())
    val newAnalysis = analyzeDistrict(parentDistrict.data(), editor)
    districtAnalysisData[parentDistrict.id] = newAnalysis
}

private fun getBestMergeCandidate(
    superdistricts: Map<SuperDistrictID, SuperDistrict>,
    districtAnalysisData: Map<SuperDistrictID, DistrictAnalysis>,
    target: SuperDistrictID,
    options: List<SuperDistrictID>
): SuperDistrictID? {
    return options
        .filter { other ->
            logger.info("Target district is ${target.value} and border is ${superdistricts[target]?.isBorder()}")
            logger.info("Other district in superdistrict ${superdistricts.containsKey(other)}")
            superdistricts.containsKey(other) &&
                    superdistricts[other]?.isBorder() == superdistricts[target]?.isBorder()
        }
        .map { other ->
            val score = getCandidateScore(
                superdistricts,
                districtAnalysisData,
                target,
                other,
                true
            )
            logger.info("Candidate ${other.value} has score $score")
            other to score
        }
        .filter { (_, score) -> score > 0.33 }
        .maxByOrNull { (_, score) -> score }
        ?.also { (other, _) ->
            logger.info("Best candidate is ${other.value}")
        }?.first
}

fun getCandidateScore(
    districts: Map<SuperDistrictID, SuperDistrict>,
    districtAnalysisData: Map<SuperDistrictID, DistrictAnalysis>,
    target: SuperDistrictID,
    candidate: SuperDistrictID,
    useAdjacency: Boolean
): Float {
    val targetAnalysis = districtAnalysisData[target]
        ?: error("Could not find district analysis data for target")
    val candidateAnalysis = districtAnalysisData[candidate]
        ?: error("Could not find district analysis data for candidate")
    val targetDistrict = districts[target]
        ?: error("Could not find district with id")
    val candidateDistrict = districts[candidate]
        ?: error("Could not find district with id")

    val adjacencyRatio = (targetDistrict.districtAdjacency()[candidateDistrict.id] ?: 0).toFloat() /
            targetDistrict.adjacenciesCount().toFloat()
    logger.info("Adjacency ratio for ${targetDistrict.id.value} and ${candidateDistrict.id.value} is $adjacencyRatio")

    val adjacencyScore = if (useAdjacency) {
        1000.0f * adjacencyRatio / candidateDistrict.size().toFloat()
    } else {
        0.0f
    }

    val biomeScore = 1.0f - targetAnalysis.biomeCount.keys.sumOf { biome ->
        abs(targetAnalysis.biomePercentage(biome) - candidateAnalysis.biomePercentage(biome)).toDouble()
    }.toFloat() / targetAnalysis.biomeCount.size.toFloat()

    val waterScore = 1.0f - abs(targetAnalysis.waterPercentage - candidateAnalysis.waterPercentage)
    val forestScore = 1.0f - abs(targetAnalysis.forestedPercentage - candidateAnalysis.forestedPercentage)
    val gradientScore = 1.0f - abs(targetAnalysis.gradient - candidateAnalysis.gradient)
    val roughnessScore = 1.0f - abs(targetAnalysis.roughness - candidateAnalysis.roughness)

    return (adjacencyScore * ADJACENCY_WEIGHT +
            biomeScore +
            waterScore +
            forestScore +
            gradientScore +
            roughnessScore) / (5.0f + if (useAdjacency) ADJACENCY_WEIGHT else 0.0f)
}

fun districtSimilarityScore(
    districtAnalysisData: Map<DistrictID, DistrictAnalysis>,
    target: DistrictID,
    candidate: DistrictID
): Float {
    val targetAnalysis = districtAnalysisData[target]
        ?: error("Could not find district analysis data for target")
    val candidateAnalysis = districtAnalysisData[candidate]
        ?: error("Could not find district analysis data for candidate")

    val biomeScore = 1.0f - targetAnalysis.biomeCount.keys.sumOf { biome ->
        abs(targetAnalysis.biomePercentage(biome) - candidateAnalysis.biomePercentage(biome)).toDouble()
    }.toFloat() / targetAnalysis.biomeCount.size.toFloat()

    val waterScore = 1.0f - abs(targetAnalysis.waterPercentage - candidateAnalysis.waterPercentage)
    val forestScore = 1.0f - abs(targetAnalysis.forestedPercentage - candidateAnalysis.forestedPercentage)
    val gradientScore = 1.0f - abs(targetAnalysis.gradient - candidateAnalysis.gradient)
    val roughnessScore = 1.0f - abs(targetAnalysis.roughness - candidateAnalysis.roughness)

    return (biomeScore + waterScore + forestScore + gradientScore + roughnessScore) / 5.0f
}
