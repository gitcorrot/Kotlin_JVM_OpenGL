package interfaces

import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3

interface Rotatable {

    var rotation: Mat4

    // TODO: Implement quaternions
    fun rotateBy(yaw: Float, pitch: Float, roll: Float) {
        rotation
            .rotate_(glm.radians(yaw), Vec3(0f, 1f, 0f))
            .rotate_(glm.radians(pitch), Vec3(1f, 0f, 0f))
            .rotate_(glm.radians(roll), Vec3(0f, 0f, 1f))
    }

    fun rotateTo(yaw: Float, pitch: Float, roll: Float) {
        rotation = Mat4()
            .rotate_(glm.radians(yaw), Vec3(0f, 1f, 0f))
            .rotate_(glm.radians(pitch), Vec3(1f, 0f, 0f))
            .rotate_(glm.radians(roll), Vec3(0f, 0f, 1f))
    }
}
