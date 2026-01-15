package noise

import geometry.Point2D
import geometry.Point3D
import java.security.MessageDigest

/**
 * A deterministic random number generator using the Squirrel3 algorithm.
 * 
 * @property seed The seed value for this RNG
 * @property state The current state counter
 */
class RNG(
    private val seed: Seed,
    private var state: Long = 0
) {
    companion object {
        private const val BIT_NOISE1: Long = 0x85297A4D
        private const val BIT_NOISE2: Long = 0x68E31DA4
        private const val BIT_NOISE3: Long = 0x1859C4E9

        /**
         * Creates a new RNG from a seed.
         */
        fun new(seed: Seed): RNG = RNG(seed)

        /**
         * Creates an RNG from a seed and a string.
         * The string is hashed and XORed with the seed.
         * Note: Uses simple byte folding instead of Rust's DefaultHasher for portability.
         */
        fun fromSeedAndString(seed: Seed, string: String): RNG {
            val hash = string.toByteArray().fold(0L) { acc, byte ->
                (acc * 31 + byte.toLong()) and -1L
            }
            return RNG(Seed(seed.value xor hash))
        }

        /**
         * Squirrel3 noise function for deterministic pseudo-random number generation.
         * Note: Line 48 uses XOR instead of multiplication to match the Rust implementation.
         */
        private fun squirrel3(seed: Seed, position: Long): Long {
            var noise = position
            noise *= BIT_NOISE1
            noise += seed.value
            noise = noise xor (noise shr 8)
            noise += BIT_NOISE2
            noise = noise xor (noise shl 8)
            noise = noise xor BIT_NOISE3  // Rust comment says "Should be *=" but uses XOR
            noise = noise xor (noise shr 8)
            return noise
        }
    }

    /**
     * Derives a new RNG from this one with a new seed based on the next random value.
     */
    fun derive(): RNG {
        return RNG(Seed(nextI64()))
    }

    /**
     * Generates the next 64-bit random integer.
     */
    fun nextI64(): Long {
        state += 1
        return squirrel3(seed, state)
    }

    /**
     * Generates a random 32-bit integer in the range [0, max).
     * 
     * @param max The exclusive upper bound (must be positive)
     * @return A random integer in [0, max)
     */
    fun randI32(max: Int): Int {
        return ((nextI64() and 0x7FFFFFFF) % max).toInt()
    }

    /**
     * Generates a random 32-bit integer in the range [min, max).
     * 
     * @param min The inclusive lower bound
     * @param max The exclusive upper bound
     * @return A random integer in [min, max)
     */
    fun randI32Range(min: Int, max: Int): Int {
        val range = max - min
        return ((nextI64() and 0x7FFFFFFF) % range).toInt() + min
    }

    /**
     * Generates a random 2D point with coordinates in [0, max.x) and [0, max.y).
     */
    fun randPoint2d(max: Point2D): Point2D {
        val x = randI32(max.x)
        val y = randI32(max.y)
        return Point2D.new(x, y)
    }

    /**
     * Generates a random 2D point with coordinates in [min, max) for each axis.
     */
    fun randPoint2dRange(min: Point2D, max: Point2D): Point2D {
        val x = randI32(max.x - min.x) + min.x
        val y = randI32(max.y - min.y) + min.y
        return Point2D.new(x, y)
    }

    /**
     * Generates a random 3D point with coordinates in [0, max.x), [0, max.y), [0, max.z).
     */
    fun randPoint3d(max: Point3D): Point3D {
        val x = randI32(max.x)
        val y = randI32(max.y)
        val z = randI32(max.z)
        return Point3D.new(x, y, z)
    }

    /**
     * Generates a random 3D point with coordinates in [min, max) for each axis.
     */
    fun randPoint3dRange(min: Point3D, max: Point3D): Point3D {
        val x = randI32(max.x - min.x) + min.x
        val y = randI32(max.y - min.y) + min.y
        val z = randI32(max.z - min.z) + min.z
        return Point3D.new(x, y, z)
    }

    /**
     * Chooses a random element from the given list.
     * 
     * @throws IllegalArgumentException if the list is empty
     */
    fun <T> choose(options: List<T>): T {
        require(options.isNotEmpty()) { "Cannot choose from empty list" }
        val index = randI32(options.size)
        return options[index]
    }

    /**
     * Removes and returns a random element from the given mutable list.
     * 
     * @return The removed element, or null if the list is empty
     */
    fun <T> pop(options: MutableList<T>): T? {
        if (options.isEmpty()) {
            return null
        }
        val index = randI32(options.size)
        return options.removeAt(index)
    }

    /**
     * Chooses a random key from the weighted map.
     * The probability of choosing a key is proportional to its weight.
     * 
     * @throws IllegalArgumentException if the map is empty
     */
    fun <T> chooseWeighted(options: Map<T, Float>): T {
        require(options.isNotEmpty()) { "Cannot choose from empty map" }
        val totalWeight = options.values.sum()
        var randValue = randI32(100000).toFloat() / 100000.0f * totalWeight
        for ((item, weight) in options) {
            if (randValue < weight) {
                return item
            }
            randValue -= weight
        }
        return options.keys.last()
    }

    /**
     * Removes and returns a random key-value pair from the weighted map.
     * The probability of choosing a key is proportional to its weight.
     * 
     * @return The removed key-value pair, or null if the map is empty
     */
    fun <T> popWeighted(options: MutableMap<T, Float>): Pair<T, Float>? {
        if (options.isEmpty()) {
            return null
        }
        val totalWeight = options.values.sum()
        var randValue = randI32(100000).toFloat() / 100000.0f * totalWeight
        for ((item, weight) in options) {
            if (randValue < weight) {
                options.remove(item)
                return Pair(item, weight)
            }
            randValue -= weight
        }
        val lastEntry = options.entries.last()
        val result = Pair(lastEntry.key, lastEntry.value)
        options.remove(lastEntry.key)
        return result
    }

    /**
     * Returns true with probability successes/total.
     * 
     * @param successes The number of successful outcomes
     * @param total The total number of outcomes
     * @return true if the random outcome is a success
     */
    fun chance(successes: Int, total: Int): Boolean {
        if (total == 0) {
            return false
        }
        val randValue = randI32(total)
        return randValue < successes
    }

    /**
     * Returns true with the given percentage probability.
     * 
     * @param percent The success probability as a percentage (0-100)
     * @return true if the random outcome is a success
     * @throws IllegalArgumentException if percent is not in range [0, 100]
     */
    fun percent(percent: Int): Boolean {
        require(percent in 0..100) { "Percent must be between 0 and 100" }
        val randValue = randI32(100)
        return randValue < percent
    }

    /**
     * Shuffles the given integer array in place using the Fisher-Yates algorithm.
     */
    fun shuffle(items: IntArray) {
        for (i in items.size - 1 downTo 1) {
            val j = randI32(i)
            val temp = items[i]
            items[i] = items[j]
            items[j] = temp
        }
    }
}
