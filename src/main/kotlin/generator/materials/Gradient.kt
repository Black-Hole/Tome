package generator.materials

import geometry.Point3D
import noise.Seed
import kotlin.math.floor

class PerlinSettings(
    private val seed: Seed,
    private val octaves: Int,
    private val baseFrequency: Float,
    private val frequencyMultiplier: Float,
    private val weightMultiplier: Float
) {
    fun get(point: Point3D): Float {
        val x = point.x.toFloat() * baseFrequency
        val y = point.y.toFloat() * baseFrequency
        val z = point.z.toFloat() * baseFrequency

        var value = 0.0f
        var frequency = 1.0f
        var weight = 1.0f

        for (i in 0 until octaves) {
            value += weight * perlinNoise(
                x * frequency,
                y * frequency,
                z * frequency
            )
            frequency *= frequencyMultiplier
            weight *= weightMultiplier
        }

        return value
    }

    private fun perlinNoise(x: Float, y: Float, z: Float): Float {
        val xi = floor(x).toInt() and 255
        val yi = floor(y).toInt() and 255
        val zi = floor(z).toInt() and 255

        val xf = x - floor(x)
        val yf = y - floor(y)
        val zf = z - floor(z)

        val u = fade(xf)
        val v = fade(yf)
        val w = fade(zf)

        val aaa = hash(hash(hash(xi) + yi) + zi)
        val aba = hash(hash(hash(xi) + yi + 1) + zi)
        val aab = hash(hash(hash(xi) + yi) + zi + 1)
        val abb = hash(hash(hash(xi) + yi + 1) + zi + 1)
        val baa = hash(hash(hash(xi + 1) + yi) + zi)
        val bba = hash(hash(hash(xi + 1) + yi + 1) + zi)
        val bab = hash(hash(hash(xi + 1) + yi) + zi + 1)
        val bbb = hash(hash(hash(xi + 1) + yi + 1) + zi + 1)

        val x1 = lerp(grad(aaa, xf, yf, zf), grad(baa, xf - 1, yf, zf), u)
        val x2 = lerp(grad(aba, xf, yf - 1, zf), grad(bba, xf - 1, yf - 1, zf), u)
        val y1 = lerp(x1, x2, v)

        val x3 = lerp(grad(aab, xf, yf, zf - 1), grad(bab, xf - 1, yf, zf - 1), u)
        val x4 = lerp(grad(abb, xf, yf - 1, zf - 1), grad(bbb, xf - 1, yf - 1, zf - 1), u)
        val y2 = lerp(x3, x4, v)

        return lerp(y1, y2, w)
    }

    private fun hash(i: Int): Int {
        return ((i * 2654435761L + seed.value) and 0xFF).toInt()
    }

    private fun fade(t: Float): Float = t * t * t * (t * (t * 6 - 15) + 10)

    private fun lerp(a: Float, b: Float, t: Float): Float = a + t * (b - a)

    private fun grad(hash: Int, x: Float, y: Float, z: Float): Float {
        val h = hash and 15
        val u = if (h < 8) x else y
        val v = if (h < 4) y else if (h == 12 || h == 14) x else z
        return (if ((h and 1) == 0) u else -u) + (if ((h and 2) == 0) v else -v)
    }

    companion object {
        fun large(seed: Seed): PerlinSettings = PerlinSettings(
            seed = seed,
            octaves = 8,
            baseFrequency = 7.0f / 32.0f,
            frequencyMultiplier = 2.0f,
            weightMultiplier = 0.5f
        )

        fun medium(seed: Seed): PerlinSettings = PerlinSettings(
            seed = seed,
            octaves = 8,
            baseFrequency = 10.0f / 32.0f,
            frequencyMultiplier = 2.0f,
            weightMultiplier = 0.5f
        )

        fun small(seed: Seed): PerlinSettings = PerlinSettings(
            seed = seed,
            octaves = 8,
            baseFrequency = 16.0f / 32.0f,
            frequencyMultiplier = 2.0f,
            weightMultiplier = 0.5f
        )
    }
}

class GradientAxis(
    private val min: Int,
    private val max: Int
) {
    fun getValue(value: Float): Float {
        val range = max - min
        if (range == 0) {
            return 0.0f
        }
        return ((value - min) / range).coerceIn(0.0f, 1.0f)
    }
}

class Gradient(
    private val perlin: PerlinSettings,
    private val gradientStrength: Float,
    private val noiseStrength: Float
) {
    private var xAxis: GradientAxis? = null
    private var yAxis: GradientAxis? = null
    private var zAxis: GradientAxis? = null

    fun withX(min: Int, max: Int): Gradient {
        xAxis = GradientAxis(min, max)
        return this
    }

    fun withY(min: Int, max: Int): Gradient {
        yAxis = GradientAxis(min, max)
        return this
    }

    fun withZ(min: Int, max: Int): Gradient {
        zAxis = GradientAxis(min, max)
        return this
    }

    fun getValue(point: Point3D): Float {
        var value = 0.0f
        var axisCount = 0

        xAxis?.let {
            value += it.getValue(point.x.toFloat())
            axisCount++
        }
        yAxis?.let {
            value += it.getValue(point.y.toFloat())
            axisCount++
        }
        zAxis?.let {
            value += it.getValue(point.z.toFloat())
            axisCount++
        }

        // Normalize the value to be between 0.0 and 1.0
        if (axisCount > 0) {
            value /= axisCount
        }

        // Apply the gradient strength
        val perlinValue = perlin.get(point)
        value = lerp(value, perlinValue, noiseStrength)

        return (value.coerceIn(0.0f, 1.0f) * gradientStrength + 0.5f * (1.0f - gradientStrength))
    }

    private fun lerp(a: Float, b: Float, t: Float): Float = a + t * (b - a)
}
