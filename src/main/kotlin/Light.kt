import glm_.vec3.Vec3

open class Light() {

    var color = Vec3(1.0f)
    var intensity = 1.0f
        set(value) {
            field = value
            color = color * value
        }

    open fun apply(shaderProgram: ShaderProgram) {}
}