package interfaces

import glm_.vec3.Vec3

interface Movable {

    val position: Vec3

    fun moveBy(x: Float, y: Float, z: Float) {
        position.x += x
        position.y += y
        position.z += z
    }

    fun moveBy(v: Vec3) {
        position.plusAssign(v)
    }

    fun moveTo(x: Float, y: Float, z: Float) {
        position.x = x
        position.y = y
        position.z = z
    }

    fun moveTo(v: Vec3) {
        position.x = v.x
        position.y = v.y
        position.z = v.z
    }
}
