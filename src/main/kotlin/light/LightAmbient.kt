package light

import ShaderProgram

class LightAmbient : Light() {

    override fun apply(shaderProgram: ShaderProgram) {
        shaderProgram.setUniformVec3f("ambientLight.color", color)
    }
}