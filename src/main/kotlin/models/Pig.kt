package models

import Texture
import models.base.ModelDefault
import utils.ModelLoader

class Pig : ModelDefault() {

    companion object {
        // TODO: Enum with meshes
        val pigMesh = ModelLoader.loadStaticModel("src/main/resources/Models/pig.obj")
    }

    init {
        super.addMesh(pigMesh)
        addTexture(Texture.getDefaultColorPalette())
        super.create()
    }
}