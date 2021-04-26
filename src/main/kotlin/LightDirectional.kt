import glm_.vec3.Vec3

class LightDirectional() : Light() {

    var direction = Vec3(0.0f)

    override fun apply(shaderProgram: ShaderProgram) {
        super.apply(shaderProgram)

        shaderProgram.setUniformVec3f("directionalLight.color", color)
        shaderProgram.setUniformVec3f("directionalLight.direction", direction)
    }
}