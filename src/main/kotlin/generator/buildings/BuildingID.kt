package generator.buildings

data class BuildingID(val value: Int) {
    companion object {
        fun from(id: Int): BuildingID = BuildingID(id)
    }
}
