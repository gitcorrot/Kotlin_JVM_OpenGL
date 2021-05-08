package interfaces

import glm_.vec3.Vec3

interface Movable {

    val translation: Vec3

    fun moveBy(x: Float, y: Float, z: Float) {
        translation.x += x
        translation.y += y
        translation.z += z
    }

    fun moveTo(x: Float, y: Float, z: Float) {
        translation.x = x
        translation.y = y
        translation.z = z
    }

    fun moveTo(v: Vec3) {
        translation.x = v.x
        translation.y = v.y
        translation.z = v.z
    }
}
