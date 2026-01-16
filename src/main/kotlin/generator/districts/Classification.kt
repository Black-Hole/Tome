package generator.districts

import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Classification")

fun classifyDistricts(
    districts: MutableMap<DistrictID, District>,
    districtAnalysisData: Map<DistrictID, DistrictAnalysis>
) {
    val options = mutableListOf<DistrictID>()

    for ((id, district) in districts) {
        val analysisData = districtAnalysisData[id]
            ?: error("District analysis data not found")

        if (district.data.isBorder ||
            analysisData.roughness > OFF_LIMITS_ROUGHNESS ||
            analysisData.gradient > OFF_LIMITS_GRADIENT
        ) {
            district.data.districtType = DistrictType.OffLimits
            logger.info("District $id is off-limits due to roughness or gradient")
            continue
        }
        if (analysisData.waterPercentage <= URBAN_WATER_LIMIT &&
            district.data.districtType == DistrictType.Unknown
        ) {
            options.add(id)
        }
    }

    logger.info("Options for prime urban district: $options")
    val primeUrbanDistrict = selectPrimeUrbanDistrict(options, districtAnalysisData)
        ?: error("No prime urban candidate found")

    districts[primeUrbanDistrict]?.let {
        it.data.districtType = DistrictType.Urban
    } ?: error("District not found")

    val districtIds = districts.keys.toList()
    for (id in districtIds) {
        val district = districts[id] ?: error("District not found")
        if (district.data.districtType == DistrictType.Unknown) {
            val score = districtSimilarityScore(districtAnalysisData, primeUrbanDistrict, id)
            if (score > URBAN_RELATIVE_TO_PRIME) {
                district.data.districtType = DistrictType.Urban
                logger.info("District $id classified as Urban with score $score")
            } else {
                district.data.districtType = DistrictType.Rural
                logger.info("District $id classified as Rural with score $score")
            }
        }
    }
}

fun classifySuperdistricts(
    superdistricts: MutableMap<SuperDistrictID, SuperDistrict>,
    districts: MutableMap<DistrictID, District>,
    districtAnalysisData: Map<SuperDistrictID, DistrictAnalysis>
) {
    val options = mutableListOf<SuperDistrictID>()

    for ((id, superdistrict) in superdistricts) {
        if (superdistrict.data.isBorder) {
            superdistrict.data.districtType = DistrictType.OffLimits
            logger.info("Superdistrict $id is off-limits due to being border")
            continue
        }
        val score = superdistrictScore(superdistrict, districts)
        when {
            score <= 0.5 -> {
                logger.info("Superdistrict $id classified as Urban option with score $score")
                options.add(id)
            }
            score in 0.5..1.5 -> {
                superdistrict.data.districtType = DistrictType.Rural
                logger.info("Superdistrict $id classified as Rural with score $score")
            }
            else -> {
                superdistrict.data.districtType = DistrictType.OffLimits
                logger.info("Superdistrict $id classified as Off-Limits with score $score")
            }
        }
    }

    logger.info("Options for prime urban district: $options")
    val primeUrbanDistrict = selectPrimeUrbanSuperdistrict(options, districtAnalysisData)
        ?: error("No prime urban candidate found")

    superdistricts[primeUrbanDistrict]?.data?.districtType = DistrictType.Urban
    classifyUrbanDistricts(primeUrbanDistrict, superdistricts, districts, districtAnalysisData)

    for (district in superdistricts.values) {
        if (district.data.districtType == DistrictType.Unknown) {
            district.data.districtType = DistrictType.Rural
        }
    }
}

private fun classifyUrbanDistricts(
    primeUrbanDistrict: SuperDistrictID,
    superdistricts: MutableMap<SuperDistrictID, SuperDistrict>,
    districts: MutableMap<DistrictID, District>,
    districtAnalysisData: Map<SuperDistrictID, DistrictAnalysis>
) {
    val urbanDistricts = mutableListOf(primeUrbanDistrict)
    var urbanCount = 1

    while (urbanCount < URBAN_SIZE) {
        val options = mutableListOf<SuperDistrictID>()
        for (id in urbanDistricts.toList()) {
            val neighbours = superdistricts[id]?.data()?.districtAdjacency?.keys
                ?.filter { neighbourId ->
                    superdistricts[neighbourId]?.data()?.districtType == DistrictType.Unknown
                } ?: emptyList()
            options.addAll(neighbours)
        }

        logger.info("Options for urban district classification: $options")
        val bestCandidate = options
            .map { id ->
                val score = getCandidateScore(
                    superdistricts,
                    districtAnalysisData,
                    primeUrbanDistrict,
                    id,
                    true
                )
                logger.info("Candidate $id has score $score")
                id to score
            }
            .maxByOrNull { (_, score) -> score }
            ?.also { (other, _) ->
                logger.info("Best candidate is $other")
            }?.first

        if (bestCandidate == null) {
            logger.info("No more candidates found, stopping urban district classification.")
            break
        } else {
            superdistricts[bestCandidate]?.data?.districtType = DistrictType.Urban
        }
        urbanCount++
        urbanDistricts.add(bestCandidate)
    }
}

private fun selectPrimeUrbanDistrict(
    options: List<DistrictID>,
    districtAnalysisData: Map<DistrictID, DistrictAnalysis>
): DistrictID? {
    return options
        .map { id ->
            val analysisData = districtAnalysisData[id]
                ?: error("District analysis data not found")
            val score = urbanDistrictScore(analysisData)
            id to score
        }
        .minByOrNull { (_, score) -> score }
        ?.also { (other, _) ->
            logger.info("Best candidate is $other")
        }?.first
}

private fun selectPrimeUrbanSuperdistrict(
    options: List<SuperDistrictID>,
    districtAnalysisData: Map<SuperDistrictID, DistrictAnalysis>
): SuperDistrictID? {
    return options
        .map { id ->
            val analysisData = districtAnalysisData[id]
                ?: error("District analysis data not found")
            val score = urbanDistrictScore(analysisData)
            id to score
        }
        .minByOrNull { (_, score) -> score }
        ?.also { (other, _) ->
            logger.info("Best candidate is $other")
        }?.first
}

private fun urbanDistrictScore(analysisData: DistrictAnalysis): Float {
    val waterScore = analysisData.waterPercentage
    val forestScore = analysisData.forestedPercentage
    val gradientScore = analysisData.gradient / 3.0f
    val roughnessScore = analysisData.roughness

    return waterScore + forestScore + gradientScore + roughnessScore
}

private fun superdistrictScore(
    superdistrict: SuperDistrict,
    districts: MutableMap<DistrictID, District>
): Float {
    val subTypes = superdistrict.getSubtypes(districts)
    return subTypes.entries.sumOf { (districtType, count) ->
        when (districtType) {
            DistrictType.Urban -> count.toDouble() * 0.0
            DistrictType.Rural -> count.toDouble() * 1.0
            DistrictType.OffLimits -> count.toDouble() * 2.0
            else -> 2.0
        }
    }.toFloat()
}
