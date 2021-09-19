//package models
//
//import Texture
//import models.base.ModelDefault
//import utils.ModelLoader
//
//enum class PigType(val path: String) {
//    TYPE_1("src/main/resources/Models/pig.obj")
//}
//
//class Pig(pigType: PigType) : ModelDefault() {
//
//    init {
//        val pigMesh = ModelLoader.loadStaticModel(pigType.path)
//        super.addMesh(pigMesh)
//        addTexture(Texture.getDefaultColorPalette())
//        super.create()
//    }
//}