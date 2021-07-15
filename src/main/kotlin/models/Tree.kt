package models

import Texture
import models.base.ModelDefault
import utils.ModelLoader

class Tree() : ModelDefault() {

    companion object {
        // TODO: Enum with meshes
        val tree1Mesh = ModelLoader.loadStaticModel("src/main/resources/Models/tree1.obj")
    }

    init {
        super.addMesh(tree1Mesh)
        addTexture(Texture.getDefaultColorPalette())
        super.create()
    }
}