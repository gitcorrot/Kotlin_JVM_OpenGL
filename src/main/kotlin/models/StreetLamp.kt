//package models
//
//import Texture
//import models.base.ModelDefault
//import utils.ModelLoader
//
//class StreetLamp : ModelDefault() {
//
//    companion object {
//        val streetLampMesh = ModelLoader.loadStaticModel("src/main/resources/Models/street_lamp.obj")
//    }
//
//    init {
//        super.addMesh(streetLampMesh)
//        addTexture(Texture.getDefaultColorPalette())
//        super.create()
//    }
//}