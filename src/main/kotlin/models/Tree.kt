package models

import Texture
import models.base.ModelDefault
import utils.ModelLoader

enum class TreeType(val path: String) {
    TYPE_1("src/main/resources/Models/tree1.obj"),
    TYPE_2("src/main/resources/Models/tree2.obj"),
    TYPE_3("src/main/resources/Models/tree3.obj")
}

class Tree(treeType: TreeType) : ModelDefault() {

    init {
        val treeMesh = ModelLoader.loadStaticModel(treeType.path)
        super.addMesh(treeMesh)
        addTexture(Texture.getDefaultColorPalette())
        super.create()
    }
}