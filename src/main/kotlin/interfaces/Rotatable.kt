package interfaces

import glm_.quat.Quat
import glm_.vec3.Vec3

interface Rotatable {

    var rotation: Quat

    fun rotateYawBy(angle: Float) {
        rotation.angleAxis_(angle, Vec3(0, 1, 0))
    }

    fun rotatePitchBy(angle: Float) {
        rotation.angleAxis_(angle, Vec3(1, 0, 0))
    }

    fun rotateRollBy(angle: Float) {
        rotation.angleAxis_(angle, Vec3(0, 0, 1))
    }

    fun rotateBy(yaw: Float, pitch: Float, roll: Float) {
        rotation.times_(Quat(Vec3(pitch, yaw, roll))).normalize_()
    }

    fun rotateTo(yaw: Float, pitch: Float, roll: Float) {
        rotation = Quat(Vec3(pitch, yaw, roll)).normalize()
    }
}

