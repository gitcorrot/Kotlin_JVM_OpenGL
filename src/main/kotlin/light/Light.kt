package light

import ShaderProgram
import glm_.vec3.Vec3

abstract class Light {

    var color = Vec3(1.0f)
    var intensity = 1.0f
        set(value) {
            field = value
            color = color * value
        }

    abstract fun apply(shaderProgram: ShaderProgram)
}