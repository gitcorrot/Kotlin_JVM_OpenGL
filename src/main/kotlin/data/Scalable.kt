package data

import glm_.vec3.Vec3

data class Scalable(val scale: Vec3 = Vec3(1f, 1f, 1f)) {

    fun scaleBy(x: Float, y: Float, z: Float) {
        this.scale.x += x
        this.scale.y += y
        this.scale.z += z
    }

    fun scaleTo(x: Float, y: Float, z: Float) {
        this.scale.x = x
        this.scale.y = y
        this.scale.z = z
    }

    fun scaleTo(scale: Float) {
        this.scale.x = scale
        this.scale.y = scale
        this.scale.z = scale
    }
}
