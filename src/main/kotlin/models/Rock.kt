package models

import Texture
import models.base.ModelDefault
import utils.ModelLoader

enum class RockType(val path: String) {
    TYPE_1("src/main/resources/Models/rock1.obj"),
    TYPE_2("src/main/resources/Models/rock2.obj"),
    TYPE_3("src/main/resources/Models/rock3.obj")
}

class Rock(rockType: RockType) : ModelDefault() {

    init {
        val rockMesh = ModelLoader.loadStaticModel(rockType.path)
        super.addMesh(rockMesh)
        addTexture(Texture.getDefaultColorPalette())
        super.create()
    }
}