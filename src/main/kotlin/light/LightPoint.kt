package light

import ShaderProgram
import glm_.vec3.Vec3
import interfaces.Movable
import interfaces.Scalable

class LightPoint(
    val index: Int,
    override val position: Vec3
) : Light(), Movable {

    // Attenuation gains
    var kc = 0f
    var kl = 0f
    var kq = 0f

    override fun apply(shaderProgram: ShaderProgram) {
        shaderProgram.setUniformVec3f("pointLights[$index].color", color)
        shaderProgram.setUniformVec3f("pointLights[$index].position", position)
        shaderProgram.setUniformFloat("pointLights[$index].kc", kc)
        shaderProgram.setUniformFloat("pointLights[$index].kl", kl)
        shaderProgram.setUniformFloat("pointLights[$index].kq", kq)
    }
}