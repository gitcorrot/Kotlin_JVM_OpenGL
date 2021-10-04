package light

import ShaderProgram
import data.Movable

class LightPoint(
    private val index: Int,
    val movable: Movable
) : Light() {

    // Attenuation gains
    var kc = 0f
    var kl = 0f
    var kq = 0f

    override fun apply(shaderProgram: ShaderProgram) {
        shaderProgram.setUniformVec3f("pointLights[$index].color", color)
        shaderProgram.setUniformVec3f("pointLights[$index].position", movable.position)
        shaderProgram.setUniformFloat("pointLights[$index].kc", kc)
        shaderProgram.setUniformFloat("pointLights[$index].kl", kl)
        shaderProgram.setUniformFloat("pointLights[$index].kq", kq)
    }
}