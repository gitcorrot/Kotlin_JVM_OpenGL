package light

import ShaderProgram
import glm_.vec3.Vec3

class LightDirectional : Light() {

    var direction = Vec3(0.0f)

    override fun apply(shaderProgram: ShaderProgram) {
        shaderProgram.setUniformVec3f("directionalLight.color", color)
        shaderProgram.setUniformVec3f("directionalLight.direction", direction)
    }
}