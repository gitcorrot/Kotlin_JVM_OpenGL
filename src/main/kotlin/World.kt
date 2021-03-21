class World {

    var defaultModels: ArrayList<DefaultModel> = ArrayList()
    var lightSources: ArrayList<LightSource> = ArrayList()
//    lateinit var terrain: Terrain


    fun addDefaultModel(model: DefaultModel) {
        defaultModels.add(model)
    }

    fun addLightSource(lightSource: LightSource) {
        lightSources.add(lightSource)
    }

//    fun setTerrain(terrain: Terrain) {
//        this.terrain = terrain
//    }

    fun cleanup() {
        // TODO: implement
    }
}