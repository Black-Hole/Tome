package generator.data

import generator.buildings.roofs.RoofComponent
import generator.buildings.roofs.RoofSet
import generator.buildings.roofs.RoofSetId
import generator.buildings.walls.Wall
import generator.materials.Material
import generator.materials.MaterialId
import generator.materials.MaterialLoader
import generator.materials.Palette
import generator.materials.PaletteId
import generator.materials.PaletteLoader
import generator.nbts.Structure
import generator.nbts.StructureId

data class LoadedData(
    val palettes: Map<PaletteId, Palette>,
    val materials: Map<MaterialId, Material>,
    val structures: Map<StructureId, Structure>,
    val walls: Map<StructureId, Wall>,
    val roofComponents: Map<StructureId, RoofComponent>,
    val roofSets: Map<RoofSetId, RoofSet>
) {
    companion object {
        fun load(): LoadedData {
            return LoadedData(
                palettes = PaletteLoader.load(),
                materials = MaterialLoader.load(),
                structures = Structure.load(),
                walls = Wall.load(),
                roofComponents = RoofComponent.load(),
                roofSets = RoofSet.load()
            )
        }
    }
}
