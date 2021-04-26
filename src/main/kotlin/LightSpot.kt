import glm_.vec3.Vec3

class LightSpot(val index: Int) : Light() {

    var position = Vec3(0f, 0f, 0f)
    var direction = Vec3(0f, 0f, 0f)
    var innerAngle = 0f
    var outerAngle = 0f


    override fun apply(shaderProgram: ShaderProgram) {
        super.apply(shaderProgram)

        shaderProgram.setUniformVec3f("spotLights[$index].color", color)
        shaderProgram.setUniformVec3f("spotLights[$index].position", position)
        shaderProgram.setUniformVec3f("spotLights[$index].direction", direction)
        shaderProgram.setUniformFloat("spotLights[$index].innerAngle", innerAngle)
        shaderProgram.setUniformFloat("spotLights[$index].outerAngle", outerAngle)
    }
}