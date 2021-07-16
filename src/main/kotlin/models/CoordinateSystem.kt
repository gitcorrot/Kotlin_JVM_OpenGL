package models

import Texture
import models.base.ModelNoLight
import utils.ModelLoader

class CoordinateSystem : ModelNoLight() {

    companion object {
        val coordinateSystemMesh = ModelLoader.loadStaticModel("src/main/resources/Models/cs2.obj")
    }

    init {
        super.addMesh(coordinateSystemMesh)
        addTexture(Texture.getDefaultColorPalette())
        super.create()
    }
}