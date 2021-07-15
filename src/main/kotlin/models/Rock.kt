package models

import Texture
import models.base.ModelDefault
import utils.ModelLoader

class Rock() : ModelDefault() {

    companion object {
        // TODO: Enum with meshes
        val rock1Mesh = ModelLoader.loadStaticModel("src/main/resources/Models/rock1.obj")
    }

    init {
        super.addMesh(rock1Mesh)
        addTexture(Texture.getDefaultColorPalette())
        super.create()
    }
}