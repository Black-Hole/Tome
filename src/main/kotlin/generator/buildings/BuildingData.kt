package generator.buildings

import generator.materials.PaletteId
import generator.style.Style

data class BuildingData(
    val id: BuildingID,
    val grid: Grid,
    val shape: BuildingShape,
    val palette: PaletteId,
    val style: Style
)
