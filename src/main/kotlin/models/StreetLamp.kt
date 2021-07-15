package models

import Texture
import models.base.ModelDefault
import utils.ModelLoader

class StreetLamp() : ModelDefault() {

    companion object {
        // TODO: Enum with meshes
        val streetLamp1Mesh = ModelLoader.loadStaticModel("src/main/resources/Models/street_lamp.obj")
    }

    init {
        super.addMesh(streetLamp1Mesh)
        addTexture(Texture.getDefaultColorPalette())
        super.create()
    }
}