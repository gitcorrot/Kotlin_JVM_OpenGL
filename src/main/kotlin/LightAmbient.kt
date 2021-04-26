import glm_.vec3.Vec3

class LightAmbient : Light() {

    override fun apply(shaderProgram: ShaderProgram) {
        super.apply(shaderProgram)

        shaderProgram.setUniformVec3f("ambientLight.color", color)
    }
}