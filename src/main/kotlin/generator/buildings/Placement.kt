package generator.buildings

import editor.BuildClaim
import editor.Editor
import generator.materials.PaletteId
import generator.style.Style
import noise.RNG

fun placeBuilding(
    editor: Editor,
    shape: BuildingShape,
    grid: Grid,
    style: Style,
    rng: RNG,
    palette: PaletteId
) {
    val data = BuildingData(
        id = BuildingID(editor.worldMut().buildings.size),
        grid = grid,
        shape = shape,
        palette = palette,
        style = style
    )

    for (point in data.shape.getFootprint(data.grid)) {
        editor.worldMut().claim(point, BuildClaim.Building)
    }

    editor.worldMut().buildings.add(data)
}
