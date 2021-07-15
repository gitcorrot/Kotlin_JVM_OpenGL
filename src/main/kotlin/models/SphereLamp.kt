package models

import models.base.ModelNoLight
import utils.ModelLoader

class SphereLamp : ModelNoLight() {
    companion object {
        // TODO: Enum with meshes
        val sphereLampMesh = ModelLoader.loadStaticModel("src/main/resources/Models/sphere.obj")
    }

    init {
        super.addMesh(sphereLampMesh)
        addTexture(Texture.getDefaultColorPalette())
        super.create()
    }
}