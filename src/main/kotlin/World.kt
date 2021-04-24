import models.ModelDefault
import models.ModelNoLight

class World {

    var modelsDefault: ArrayList<ModelDefault> = ArrayList()
    var modelsNoLight: ArrayList<ModelNoLight> = ArrayList()
//    lateinit var terrain: Terrain

    var skybox: Skybox? = null


    fun addDefaultModel(model: ModelDefault) {
        modelsDefault.add(model)
    }

    fun addLightSource(modelNoLight: ModelNoLight) {
        modelsNoLight.add(modelNoLight)
    }

//    fun setTerrain(terrain: Terrain) {
//        this.terrain = terrain
//    }

    fun cleanup() {
        // TODO: implement
    }
}
