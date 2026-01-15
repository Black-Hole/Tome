package generator.materials

import editor.Editor
import geometry.Point3D
import minecraft.BlockForm
import noise.RNG

class Placer(
    private val materials: Map<MaterialId, Material>,
    private val rng: RNG
) {
    private var shadeFunction: ((Point3D) -> Float)? = null
    private var wetnessFunction: ((Point3D) -> Float)? = null
    private var wearFunction: ((Point3D) -> Float)? = null
    private var decorativenessFunction: ((Point3D) -> Float)? = null

    fun withShadeFunction(shadeFunction: (Point3D) -> Float): Placer {
        this.shadeFunction = shadeFunction
        return this
    }

    fun withWetnessFunction(wetnessFunction: (Point3D) -> Float): Placer {
        this.wetnessFunction = wetnessFunction
        return this
    }

    fun withWearFunction(wearFunction: (Point3D) -> Float): Placer {
        this.wearFunction = wearFunction
        return this
    }

    fun withDecorativenessFunction(decorativenessFunction: (Point3D) -> Float): Placer {
        this.decorativenessFunction = decorativenessFunction
        return this
    }

    suspend fun placeBlock(
        editor: Editor,
        point: Point3D,
        material: MaterialId,
        form: BlockForm,
        state: Map<String, String>? = null,
        data: String? = null
    ) {
        val parameters = MaterialParameters(
            shade = shadeFunction?.invoke(point) ?: 0.5f,
            wear = wearFunction?.invoke(point) ?: 0.5f,
            moisture = wetnessFunction?.invoke(point) ?: 0.5f,
            decoration = decorativenessFunction?.invoke(point) ?: 0.5f
        )

        materials[material]?.placeBlock(
            editor, point, form, materials, state, data, parameters, rng, false
        )
    }

    suspend fun placeBlockForced(
        editor: Editor,
        point: Point3D,
        material: MaterialId,
        form: BlockForm,
        state: Map<String, String>? = null,
        data: String? = null
    ) {
        val parameters = MaterialParameters(
            shade = shadeFunction?.invoke(point) ?: 0.5f,
            wear = wearFunction?.invoke(point) ?: 0.5f,
            moisture = wetnessFunction?.invoke(point) ?: 0.5f,
            decoration = decorativenessFunction?.invoke(point) ?: 0.5f
        )

        materials[material]?.placeBlock(
            editor, point, form, materials, state, data, parameters, rng, true
        )
    }

    suspend fun placeBlocks(
        editor: Editor,
        points: Iterable<Point3D>,
        material: MaterialId,
        form: BlockForm,
        state: Map<String, String>? = null,
        data: String? = null
    ) {
        for (point in points) {
            placeBlock(editor, point, material, form, state, data)
        }
    }
}

class MaterialPlacer(
    private val placer: Placer,
    private val material: MaterialId
) {
    suspend fun placeBlock(
        editor: Editor,
        point: Point3D,
        form: BlockForm,
        state: Map<String, String>? = null,
        data: String? = null
    ) {
        placer.placeBlock(editor, point, material, form, state, data)
    }

    suspend fun placeBlockForced(
        editor: Editor,
        point: Point3D,
        form: BlockForm,
        state: Map<String, String>? = null,
        data: String? = null
    ) {
        placer.placeBlockForced(editor, point, material, form, state, data)
    }

    suspend fun placeBlocks(
        editor: Editor,
        points: Iterable<Point3D>,
        form: BlockForm,
        state: Map<String, String>? = null,
        data: String? = null
    ) {
        placer.placeBlocks(editor, points, material, form, state, data)
    }
}
