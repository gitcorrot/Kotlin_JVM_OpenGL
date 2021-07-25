package interfaces

import glm_.quat.Quat
import glm_.vec3.Vec3

interface Rotatable {

    var rotation: Quat

    fun rotateYawBy(angle: Float) {
        rotateBy(yaw = angle)
    }

    fun rotatePitchBy(angle: Float) {
        rotateBy(pitch = angle)
    }

    fun rotateRollBy(angle: Float) {
        rotateBy(roll = angle)
    }

    fun rotateBy(yaw: Float = 0f, pitch: Float = 0f, roll: Float = 0f) {
        rotation.times_(Quat(Vec3(pitch, yaw, roll))).normalize_()
    }

    fun rotateTo(yaw: Float, pitch: Float, roll: Float) {
        rotation = Quat(Vec3(pitch, yaw, roll)).normalize()
    }
}

