package light

import ShaderProgram
import glm_.vec3.Vec3
import interfaces.Movable

class LightSpot(
    val index: Int,
    override val translation: Vec3
) : Light(), Movable {

    var position = Vec3(0f, 0f, 0f)
    var direction = Vec3(0f, 0f, 0f)
    var innerAngle = 0f
    var outerAngle = 0f


    override fun apply(shaderProgram: ShaderProgram) {
        shaderProgram.setUniformVec3f("spotLights[$index].color", color)
        shaderProgram.setUniformVec3f("spotLights[$index].position", translation)
        shaderProgram.setUniformVec3f("spotLights[$index].direction", direction)
        shaderProgram.setUniformFloat("spotLights[$index].innerAngle", innerAngle)
        shaderProgram.setUniformFloat("spotLights[$index].outerAngle", outerAngle)
    }
}