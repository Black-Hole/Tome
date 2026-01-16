package generator.terrain

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import org.slf4j.LoggerFactory
import editor.Editor
import geometry.Point3D
import minecraft.Block
import minecraft.stringToBlock
import noise.RNG
import noise.Seed

private val logger = LoggerFactory.getLogger("Tree")

@Serializable
enum class Tree {
    @SerialName("mega_birch")
    MEGA_BIRCH,
    @SerialName("large_birch")
    LARGE_BIRCH,
    @SerialName("medium_birch")
    MEDIUM_BIRCH,
    @SerialName("small_birch")
    SMALL_BIRCH,
    @SerialName("mega_jungle")
    MEGA_JUNGLE,
    @SerialName("large_jungle")
    LARGE_JUNGLE,
    @SerialName("medium_jungle")
    MEDIUM_JUNGLE,
    @SerialName("small_jungle")
    SMALL_JUNGLE,
    @SerialName("mega_pine")
    MEGA_PINE,
    @SerialName("large_pine")
    LARGE_PINE,
    @SerialName("medium_pine")
    MEDIUM_PINE,
    @SerialName("small_pine")
    SMALL_PINE,
    @SerialName("mega_hedge")
    MEGA_HEDGE,
    @SerialName("large_hedge")
    LARGE_HEDGE,
    @SerialName("medium_hedge")
    MEDIUM_HEDGE,
    @SerialName("small_hedge")
    SMALL_HEDGE,
    @SerialName("mega_baobab")
    MEGA_BAOBAB,
    @SerialName("large_baobab")
    LARGE_BAOBAB,
    @SerialName("medium_baobab")
    MEDIUM_BAOBAB,
    @SerialName("small_baobab")
    SMALL_BAOBAB,
    @SerialName("mega_oak")
    MEGA_OAK,
    @SerialName("large_oak")
    LARGE_OAK,
    @SerialName("medium_oak")
    MEDIUM_OAK,
    @SerialName("small_oak")
    SMALL_OAK
}

suspend fun generateTree(
    tree: Tree,
    editor: Editor,
    point: Point3D,
    rng: RNG,
    palette: Map<String, Map<String, Float>>
) {
    val newSeed = Seed(rng.nextI64())
    val newRng = RNG(newSeed)
    val wood = newRng.chooseWeighted(palette["wood"] ?: error("Wood palette not found"))
    val leaves = newRng.chooseWeighted(palette["leaves"] ?: error("Leaves palette not found"))
    val woodBlock = stringToBlock(wood) ?: error("Failed to convert wood to block")
    val leafBlock = stringToBlock(leaves) ?: error("Failed to convert leaves to block")
    logger.info("Leaf block: {}, Wood: {}", leafBlock, woodBlock)

    when (tree) {
        Tree.MEGA_BIRCH -> generateMegaBirch(editor, point, woodBlock, leafBlock, rng, 100)
        Tree.LARGE_BIRCH -> generateLargeBirch(editor, point, woodBlock, leafBlock, rng, 95)
        Tree.MEDIUM_BIRCH -> generateMediumBirch(editor, point, woodBlock, leafBlock, rng, 96)
        Tree.SMALL_BIRCH -> generateSmallBirch(editor, point, woodBlock, leafBlock, rng, 98)
        Tree.MEGA_JUNGLE -> { /* Placeholder */ }
        Tree.LARGE_JUNGLE -> { /* Placeholder */ }
        Tree.MEDIUM_JUNGLE -> { /* Placeholder */ }
        Tree.SMALL_JUNGLE -> { /* Placeholder */ }
        Tree.MEGA_PINE -> generateMegaPine(editor, point, woodBlock, leafBlock, rng, 95)
        Tree.LARGE_PINE -> generateLargePine(editor, point, woodBlock, leafBlock, rng, 95)
        Tree.MEDIUM_PINE -> generateMediumPine(editor, point, woodBlock, leafBlock, rng, 95)
        Tree.SMALL_PINE -> generateSmallPine(editor, point, woodBlock, leafBlock, rng, 100)
        Tree.MEGA_HEDGE -> generateMegaHedge(editor, point, woodBlock, leafBlock, rng, 98)
        Tree.LARGE_HEDGE -> generateLargeHedge(editor, point, woodBlock, leafBlock, rng, 100)
        Tree.MEDIUM_HEDGE -> generateMediumHedge(editor, point, woodBlock, leafBlock, rng, 100)
        Tree.SMALL_HEDGE -> generateSmallHedge(editor, point, woodBlock, leafBlock, rng, 100)
        Tree.MEGA_BAOBAB -> { /* Placeholder */ }
        Tree.LARGE_BAOBAB -> { /* Placeholder */ }
        Tree.MEDIUM_BAOBAB -> { /* Placeholder */ }
        Tree.SMALL_BAOBAB -> { /* Placeholder */ }
        Tree.MEGA_OAK -> generateMegaOak(editor, point, woodBlock, leafBlock, rng, 96)
        Tree.LARGE_OAK -> generateLargeOak(editor, point, woodBlock, leafBlock, rng, 100)
        Tree.MEDIUM_OAK -> generateMediumOak(editor, point, woodBlock, leafBlock, rng, 100)
        Tree.SMALL_OAK -> generateSmallOak(editor, point, woodBlock, leafBlock, rng, 100)
    }
}

private suspend fun generateSmallBirch(
    editor: Editor,
    point: Point3D,
    wood: Block,
    leaf: Block,
    rng: RNG,
    leafChance: Int
) {
    val (x0, y0, z0) = point
    val height = rng.randI32Range(5, 10) + y0

    for (y in y0..(height + 2)) {
        if (y >= height) {
            editor.placeBlockChance(leaf, Point3D(x0, y, z0), rng, leafChance)
        } else {
            editor.placeBlock(wood, Point3D(x0, y, z0))
        }
        val mid = ((height - y0) / 2 + y0) - 1
        if (y == mid || y == height + 1) {
            editor.placeBlockChance(leaf, Point3D(x0 + 1, y, z0), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 - 1, y, z0), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0, y, z0 + 1), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0, y, z0 - 1), rng, leafChance)
        } else if (y > mid && y < height + 2) {
            editor.placeBlockChance(leaf, Point3D(x0 + 1, y, z0 + 1), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 - 1, y, z0 - 1), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 - 1, y, z0 + 1), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 + 1, y, z0 - 1), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 + 1, y, z0), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 - 1, y, z0), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0, y, z0 + 1), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0, y, z0 - 1), rng, leafChance)
        }
    }
}

private suspend fun generateMediumBirch(
    editor: Editor,
    point: Point3D,
    wood: Block,
    leaf: Block,
    rng: RNG,
    leafChance: Int
) {
    val (x0, y0, z0) = point
    val height = rng.randI32Range(10, 16) + y0
    val branchNum = rng.randI32Range(2, 6)

    val branches = mutableListOf<Triple<Int, Int, Int>>()
    branches.add(Triple(x0, height, z0))

    // Trunk and base
    for (y in y0 until height) {
        editor.placeBlock(wood, Point3D(x0, y, z0))
        if (y == y0) {
            for ((dx, dz) in listOf(Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1))) {
                if (rng.randI32Range(1, 5) != 4) {
                    editor.placeBlock(wood, Point3D(x0 + dx, y, z0 + dz))
                }
            }
        }
    }

    // Branches
    repeat(branchNum) {
        val branchHeight = rng.randI32Range(((height - y0) / 2 + y0) + 2, height)
        val branchPos = rng.randI32Range(1, 17)

        when (branchPos) {
            1 -> {
                branches.add(Triple(x0 + 2, branchHeight, z0))
                editor.placeBlock(wood, Point3D(x0 + 2, branchHeight, z0))
                editor.placeBlock(wood, Point3D(x0 + 2, branchHeight - 1, z0))
                editor.placeBlock(wood, Point3D(x0 + 1, branchHeight - 2, z0))
            }
            2 -> {
                branches.add(Triple(x0 + 2, branchHeight, z0 + 1))
                editor.placeBlock(wood, Point3D(x0 + 2, branchHeight, z0 + 1))
                editor.placeBlock(wood, Point3D(x0 + 2, branchHeight - 1, z0 + 1))
                if (rng.randI32Range(1, 3) == 1) {
                    editor.placeBlock(wood, Point3D(x0 + 1, branchHeight - 2, z0 + 1))
                } else {
                    editor.placeBlock(wood, Point3D(x0 + 1, branchHeight - 2, z0))
                }
            }
            3 -> {
                branches.add(Triple(x0 + 2, branchHeight, z0 + 2))
                editor.placeBlock(wood, Point3D(x0 + 2, branchHeight, z0 + 2))
                editor.placeBlock(wood, Point3D(x0 + 2, branchHeight - 1, z0 + 2))
                editor.placeBlock(wood, Point3D(x0 + 1, branchHeight - 2, z0 + 1))
            }
            4 -> {
                branches.add(Triple(x0 + 2, branchHeight, z0 - 1))
                editor.placeBlock(wood, Point3D(x0 + 2, branchHeight, z0 - 1))
                editor.placeBlock(wood, Point3D(x0 + 2, branchHeight - 1, z0 - 1))
                if (rng.randI32Range(1, 3) == 1) {
                    editor.placeBlock(wood, Point3D(x0 + 1, branchHeight - 2, z0 - 1))
                } else {
                    editor.placeBlock(wood, Point3D(x0 + 1, branchHeight - 2, z0))
                }
            }
            5 -> {
                branches.add(Triple(x0 + 2, branchHeight, z0 - 2))
                editor.placeBlock(wood, Point3D(x0 + 2, branchHeight, z0 - 2))
                editor.placeBlock(wood, Point3D(x0 + 2, branchHeight - 1, z0 - 2))
                editor.placeBlock(wood, Point3D(x0 + 1, branchHeight - 2, z0 - 1))
            }
            6 -> {
                branches.add(Triple(x0 - 2, branchHeight, z0))
                editor.placeBlock(wood, Point3D(x0 - 2, branchHeight, z0))
                editor.placeBlock(wood, Point3D(x0 - 2, branchHeight - 1, z0))
                editor.placeBlock(wood, Point3D(x0 - 1, branchHeight - 2, z0))
            }
            7 -> {
                branches.add(Triple(x0 - 2, branchHeight, z0 + 1))
                editor.placeBlock(wood, Point3D(x0 - 2, branchHeight, z0 + 1))
                editor.placeBlock(wood, Point3D(x0 - 2, branchHeight - 1, z0 + 1))
                if (rng.randI32Range(1, 3) == 1) {
                    editor.placeBlock(wood, Point3D(x0 - 1, branchHeight - 2, z0 + 1))
                } else {
                    editor.placeBlock(wood, Point3D(x0 - 1, branchHeight - 2, z0))
                }
            }
            8 -> {
                branches.add(Triple(x0 - 2, branchHeight, z0 + 2))
                editor.placeBlock(wood, Point3D(x0 - 2, branchHeight, z0 + 2))
                editor.placeBlock(wood, Point3D(x0 - 2, branchHeight - 1, z0 + 2))
                editor.placeBlock(wood, Point3D(x0 - 1, branchHeight - 2, z0 + 1))
            }
            9 -> {
                branches.add(Triple(x0 - 2, branchHeight, z0 - 1))
                editor.placeBlock(wood, Point3D(x0 - 2, branchHeight, z0 - 1))
                editor.placeBlock(wood, Point3D(x0 - 2, branchHeight - 1, z0 - 1))
                if (rng.randI32Range(1, 3) == 1) {
                    editor.placeBlock(wood, Point3D(x0 - 1, branchHeight - 2, z0 - 1))
                } else {
                    editor.placeBlock(wood, Point3D(x0 - 1, branchHeight - 2, z0))
                }
            }
            10 -> {
                branches.add(Triple(x0 - 2, branchHeight, z0 - 2))
                editor.placeBlock(wood, Point3D(x0 - 2, branchHeight, z0 - 2))
                editor.placeBlock(wood, Point3D(x0 - 2, branchHeight - 1, z0 - 2))
                editor.placeBlock(wood, Point3D(x0 - 1, branchHeight - 2, z0 - 1))
            }
            11 -> {
                branches.add(Triple(x0 + 1, branchHeight, z0 - 2))
                editor.placeBlock(wood, Point3D(x0 + 1, branchHeight, z0 - 2))
                editor.placeBlock(wood, Point3D(x0 + 1, branchHeight - 1, z0 - 2))
                if (rng.randI32Range(1, 3) == 1) {
                    editor.placeBlock(wood, Point3D(x0 + 1, branchHeight - 2, z0 - 1))
                } else {
                    editor.placeBlock(wood, Point3D(x0, branchHeight - 2, z0 - 1))
                }
            }
            12 -> {
                branches.add(Triple(x0, branchHeight, z0 - 2))
                editor.placeBlock(wood, Point3D(x0, branchHeight, z0 - 2))
                editor.placeBlock(wood, Point3D(x0, branchHeight - 1, z0 - 2))
                editor.placeBlock(wood, Point3D(x0, branchHeight - 2, z0 - 1))
            }
            13 -> {
                branches.add(Triple(x0 - 1, branchHeight, z0 - 2))
                editor.placeBlock(wood, Point3D(x0 - 1, branchHeight, z0 - 2))
                editor.placeBlock(wood, Point3D(x0 - 1, branchHeight - 1, z0 - 2))
                if (rng.randI32Range(1, 3) == 1) {
                    editor.placeBlock(wood, Point3D(x0 - 1, branchHeight - 2, z0 - 1))
                } else {
                    editor.placeBlock(wood, Point3D(x0, branchHeight - 2, z0 - 1))
                }
            }
            14 -> {
                branches.add(Triple(x0 + 1, branchHeight, z0 + 2))
                editor.placeBlock(wood, Point3D(x0 + 1, branchHeight, z0 + 2))
                editor.placeBlock(wood, Point3D(x0 + 1, branchHeight - 1, z0 + 2))
                if (rng.randI32Range(1, 3) == 1) {
                    editor.placeBlock(wood, Point3D(x0 + 1, branchHeight - 2, z0 + 1))
                } else {
                    editor.placeBlock(wood, Point3D(x0, branchHeight - 2, z0 + 1))
                }
            }
            15 -> {
                branches.add(Triple(x0, branchHeight, z0 + 2))
                editor.placeBlock(wood, Point3D(x0, branchHeight, z0 + 2))
                editor.placeBlock(wood, Point3D(x0, branchHeight - 1, z0 + 2))
                editor.placeBlock(wood, Point3D(x0, branchHeight - 2, z0 + 1))
            }
            16 -> {
                branches.add(Triple(x0 - 1, branchHeight, z0 + 2))
                editor.placeBlock(wood, Point3D(x0 - 1, branchHeight, z0 + 2))
                editor.placeBlock(wood, Point3D(x0 - 1, branchHeight - 1, z0 + 2))
                if (rng.randI32Range(1, 3) == 1) {
                    editor.placeBlock(wood, Point3D(x0 - 1, branchHeight - 2, z0 + 1))
                } else {
                    editor.placeBlock(wood, Point3D(x0, branchHeight - 2, z0 + 1))
                }
            }
        }
    }

    // Leaves for each branch
    for ((x1, y1, z1) in branches) {
        editor.placeBlockChance(leaf, Point3D(x1, y1 + 3, z1), rng, leafChance)

        for ((dx, dz) in listOf(Pair(0, 0), Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1))) {
            editor.placeBlockChance(leaf, Point3D(x1 + dx, y1 + 2, z1 + dz), rng, leafChance)
        }

        val thirdLayer = listOf(
            Pair(0, 0), Pair(1, 1), Pair(-1, -1), Pair(-1, 1), Pair(1, -1),
            Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1)
        )
        for ((dx, dz) in thirdLayer) {
            editor.placeBlockChance(leaf, Point3D(x1 + dx, y1 + 1, z1 + dz), rng, leafChance)
        }

        val fourthLayer = listOf(
            Pair(1, 1), Pair(-1, -1), Pair(-1, 1), Pair(1, -1),
            Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1)
        )
        for ((dx, dz) in fourthLayer) {
            editor.placeBlockChance(leaf, Point3D(x1 + dx, y1, z1 + dz), rng, leafChance)
        }

        for ((dx, dz) in listOf(Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1))) {
            editor.placeBlockChance(leaf, Point3D(x1 + dx, y1 - 1, z1 + dz), rng, leafChance)
        }
    }
}

private suspend fun generateLargeBirch(
    editor: Editor,
    point: Point3D,
    wood: Block,
    leaf: Block,
    rng: RNG,
    leafChance: Int
) {
    generateMediumBirch(editor, point, wood, leaf, rng, leafChance)
}

private suspend fun generateMegaBirch(
    editor: Editor,
    point: Point3D,
    wood: Block,
    leaf: Block,
    rng: RNG,
    leafChance: Int
) {
    generateLargeBirch(editor, point, wood, leaf, rng, leafChance)
}

private suspend fun generateSmallPine(
    editor: Editor,
    point: Point3D,
    wood: Block,
    leaf: Block,
    rng: RNG,
    leafChance: Int
) {
    val (x0, y0, z0) = point
    val height = rng.randI32Range(5, 8) + y0

    for (y in y0..(height + 1)) {
        if (y == height + 1) {
            editor.placeBlockChance(leaf, Point3D(x0, y, z0), rng, leafChance)
            continue
        }
        editor.placeBlock(wood, Point3D(x0, y, z0))

        if (y % 2 == height % 2 && y > y0 + 1) {
            val positions = listOf(Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1))
            for ((dx, dz) in positions) {
                editor.placeBlockChance(leaf, Point3D(x0 + dx, y, z0 + dz), rng, leafChance)
            }
        } else if (y == height - 1) {
            val positions = listOf(
                Pair(1, 1), Pair(-1, -1), Pair(-1, 1), Pair(1, -1),
                Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1)
            )
            for ((dx, dz) in positions) {
                editor.placeBlockChance(leaf, Point3D(x0 + dx, y, z0 + dz), rng, leafChance)
            }
        } else if (y % 2 == (height - 1) % 2 && y > y0) {
            val positions = listOf(
                Pair(2, 0), Pair(-2, 0), Pair(0, 2), Pair(0, -2),
                Pair(1, 1), Pair(-1, -1), Pair(-1, 1), Pair(1, -1),
                Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1)
            )
            for ((dx, dz) in positions) {
                editor.placeBlockChance(leaf, Point3D(x0 + dx, y, z0 + dz), rng, leafChance)
            }
        }
    }
}

private suspend fun generateMediumPine(
    editor: Editor,
    point: Point3D,
    wood: Block,
    leaf: Block,
    rng: RNG,
    leafChance: Int
) {
    val (x0, y0, z0) = point
    val height = rng.randI32Range(8, 14) + y0

    for (y in y0..(height + 1)) {
        if (y == height + 1) {
            editor.placeBlockChance(leaf, Point3D(x0, y, z0), rng, leafChance)
            continue
        }
        editor.placeBlock(wood, Point3D(x0, y, z0))

        if (y == height) {
            for ((dx, dz) in listOf(Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1))) {
                editor.placeBlockChance(leaf, Point3D(x0 + dx, y, z0 + dz), rng, leafChance)
            }
        } else if (y == height - 1) {
            val positions = listOf(
                Pair(2, 0), Pair(-2, 0), Pair(0, 2), Pair(0, -2),
                Pair(1, 1), Pair(-1, -1), Pair(-1, 1), Pair(1, -1),
                Pair(2, 1), Pair(-2, -1), Pair(-2, 1), Pair(2, -1),
                Pair(1, 2), Pair(-1, -2), Pair(-1, 2), Pair(1, -2)
            )
            for ((dx, dz) in positions) {
                editor.placeBlockChance(leaf, Point3D(x0 + dx, y, z0 + dz), rng, leafChance)
            }
        } else if (y % 2 == height % 2 && y > y0 + 2) {
            val positions = listOf(
                Pair(2, 0), Pair(-2, 0), Pair(0, 2), Pair(0, -2),
                Pair(1, 1), Pair(-1, -1), Pair(-1, 1), Pair(1, -1),
                Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1)
            )
            for ((dx, dz) in positions) {
                editor.placeBlockChance(leaf, Point3D(x0 + dx, y, z0 + dz), rng, leafChance)
            }
        } else if (y % 2 == (height - 1) % 2 && y > y0 + 1) {
            val positions = listOf(
                Pair(2, 0), Pair(-2, 0), Pair(0, 2), Pair(0, -2),
                Pair(1, 1), Pair(-1, -1), Pair(-1, 1), Pair(1, -1),
                Pair(2, 1), Pair(-2, -1), Pair(-2, 1), Pair(2, -1),
                Pair(1, 2), Pair(-1, -2), Pair(-1, 2), Pair(1, -2),
                Pair(3, 0), Pair(-3, 0), Pair(0, 3), Pair(0, -3),
                Pair(2, 2), Pair(-2, -2), Pair(-2, 2), Pair(2, -2)
            )
            for ((dx, dz) in positions) {
                editor.placeBlockChance(leaf, Point3D(x0 + dx, y, z0 + dz), rng, leafChance)
            }
        }
    }
}

private suspend fun generateLargePine(
    editor: Editor,
    point: Point3D,
    wood: Block,
    leaf: Block,
    rng: RNG,
    leafChance: Int
) {
    generateMediumPine(editor, point, wood, leaf, rng, leafChance)
}

private suspend fun generateMegaPine(
    editor: Editor,
    point: Point3D,
    wood: Block,
    leaf: Block,
    rng: RNG,
    leafChance: Int
) {
    generateLargePine(editor, point, wood, leaf, rng, leafChance)
}

private suspend fun generateSmallHedge(
    editor: Editor,
    point: Point3D,
    wood: Block,
    leaf: Block,
    rng: RNG,
    leafChance: Int
) {
    val (x0, y0, z0) = point
    val height = rng.randI32Range(4, 8) + y0

    for (y in y0..(height + 1)) {
        if (y == height + 1) {
            editor.placeBlockChance(leaf, Point3D(x0, y, z0), rng, leafChance)
            continue
        } else {
            editor.placeBlock(wood, Point3D(x0, y, z0))
        }
        if (y > y0) {
            editor.placeBlockChance(leaf, Point3D(x0 + 1, y, z0), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 - 1, y, z0), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0, y, z0 + 1), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0, y, z0 - 1), rng, leafChance)
        }
    }
}

private suspend fun generateMediumHedge(
    editor: Editor,
    point: Point3D,
    wood: Block,
    leaf: Block,
    rng: RNG,
    leafChance: Int
) {
    val (x0, y0, z0) = point
    val height = rng.randI32Range(8, 14) + y0

    for (y in y0..(height + 1)) {
        if (y == height + 1) {
            editor.placeBlockChance(leaf, Point3D(x0, y, z0), rng, leafChance)
            continue
        } else {
            editor.placeBlock(wood, Point3D(x0, y, z0))
        }
        if (y > y0) {
            editor.placeBlockChance(leaf, Point3D(x0 + 1, y, z0), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 - 1, y, z0), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0, y, z0 + 1), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0, y, z0 - 1), rng, leafChance)
        }
        if (y > y0 + 2 && y < height) {
            editor.placeBlockChance(leaf, Point3D(x0 + 1, y, z0 + 1), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 - 1, y, z0 - 1), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 - 1, y, z0 + 1), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 + 1, y, z0 - 1), rng, leafChance)
        }
    }
}

private suspend fun generateLargeHedge(
    editor: Editor,
    point: Point3D,
    wood: Block,
    leaf: Block,
    rng: RNG,
    leafChance: Int
) {
    val (x0, y0, z0) = point
    val height = rng.randI32Range(14, 20) + y0

    for (y in y0..(height + 1)) {
        if (y == height + 1) {
            editor.placeBlockChance(leaf, Point3D(x0, y, z0), rng, leafChance)
            continue
        } else {
            editor.placeBlock(wood, Point3D(x0, y, z0))
        }
        if (y > y0) {
            editor.placeBlockChance(leaf, Point3D(x0 + 1, y, z0), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 - 1, y, z0), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0, y, z0 + 1), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0, y, z0 - 1), rng, leafChance)
        }
        if (y > y0 + 2 && y < height) {
            editor.placeBlockChance(leaf, Point3D(x0 + 1, y, z0 + 1), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 - 1, y, z0 - 1), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 - 1, y, z0 + 1), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 + 1, y, z0 - 1), rng, leafChance)
        }
        if (y > y0 + 3 && y < height - 1) {
            editor.placeBlockChance(leaf, Point3D(x0 + 2, y, z0), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 - 2, y, z0), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0, y, z0 + 2), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0, y, z0 - 2), rng, leafChance)
        }
        if (y > y0 + 5 && y < height - 3) {
            editor.placeBlockChance(leaf, Point3D(x0 + 2, y, z0 + 1), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 - 2, y, z0 + 1), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 + 1, y, z0 + 2), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 + 1, y, z0 - 2), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 + 2, y, z0 - 1), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 - 2, y, z0 - 1), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 - 1, y, z0 + 2), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 - 1, y, z0 - 2), rng, leafChance)
        }
    }
}

private suspend fun generateMegaHedge(
    editor: Editor,
    point: Point3D,
    wood: Block,
    leaf: Block,
    rng: RNG,
    leafChance: Int
) {
    generateLargeHedge(editor, point, wood, leaf, rng, leafChance)
}

private suspend fun generateSmallOak(
    editor: Editor,
    point: Point3D,
    wood: Block,
    leaf: Block,
    rng: RNG,
    leafChance: Int
) {
    val (x0, y0, z0) = point
    val stemHeight = rng.randI32Range(4, 8)

    for (y in y0..(stemHeight + y0 + 1)) {
        if (y == stemHeight + y0 + 1) {
            editor.placeBlockChance(leaf, Point3D(x0, y, z0), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 + 1, y, z0), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 - 1, y, z0), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0, y, z0 + 1), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0, y, z0 - 1), rng, leafChance)
            continue
        }
        editor.placeBlock(wood, Point3D(x0, y, z0))
        val mid = (stemHeight.toFloat() / 2.0f).toInt() + y0 - 1
        if (y == mid) {
            editor.placeBlockChance(leaf, Point3D(x0 + 1, y, z0), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0 - 1, y, z0), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0, y, z0 + 1), rng, leafChance)
            editor.placeBlockChance(leaf, Point3D(x0, y, z0 - 1), rng, leafChance)
        } else if (y == stemHeight + y0 || y == (stemHeight.toFloat() / 2.0f).toInt() + y0) {
            val positions = listOf(
                Pair(2, 0), Pair(-2, 0), Pair(0, 2), Pair(0, -2),
                Pair(1, 1), Pair(-1, -1), Pair(-1, 1), Pair(1, -1),
                Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1)
            )
            for ((dx, dz) in positions) {
                editor.placeBlockChance(leaf, Point3D(x0 + dx, y, z0 + dz), rng, leafChance)
            }
        } else if (y > (stemHeight.toFloat() / 2.0f).toInt() + y0 && y < stemHeight + y0) {
            val positions = listOf(
                Pair(2, 0), Pair(-2, 0), Pair(0, 2), Pair(0, -2),
                Pair(2, 1), Pair(-2, 1), Pair(1, 2), Pair(1, -2),
                Pair(2, -1), Pair(-2, -1), Pair(-1, 2), Pair(-1, -2),
                Pair(1, 1), Pair(-1, -1), Pair(-1, 1), Pair(1, -1),
                Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1)
            )
            for ((dx, dz) in positions) {
                editor.placeBlockChance(leaf, Point3D(x0 + dx, y, z0 + dz), rng, leafChance)
            }
        }
    }
}

private suspend fun generateMediumOak(
    editor: Editor,
    point: Point3D,
    wood: Block,
    leaf: Block,
    rng: RNG,
    leafChance: Int
) {
    generateSmallOak(editor, point, wood, leaf, rng, leafChance)
}

private suspend fun generateLargeOak(
    editor: Editor,
    point: Point3D,
    wood: Block,
    leaf: Block,
    rng: RNG,
    leafChance: Int
) {
    generateMediumOak(editor, point, wood, leaf, rng, leafChance)
}

private suspend fun generateMegaOak(
    editor: Editor,
    point: Point3D,
    wood: Block,
    leaf: Block,
    rng: RNG,
    leafChance: Int
) {
    generateLargeOak(editor, point, wood, leaf, rng, leafChance)
}
