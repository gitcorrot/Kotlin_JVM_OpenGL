package data

import glm_.quat.Quat
import glm_.vec3.Vec3

data class Rotatable(var rotation: Quat = Quat()) {

    fun rotatePitchBy(angle: Float) {
        rotateBy(angle, Vec3(1f, 0f, 0f))
    }

    fun rotateYawBy(angle: Float) {
        rotateBy(angle, Vec3(0f, 1f, 0f))
    }

    fun rotateRollBy(angle: Float) {
        rotateBy(angle, Vec3(0f, 0f, 1f))
    }

    fun rotateBy(angle: Float, axis: Vec3) {
        rotateBy(Quat.angleAxis(angle, axis))
    }

    fun rotateBy(quat: Quat) {
        rotation = quat.times(rotation).normalize()
    }

    fun rotateTo(yaw: Float, pitch: Float, roll: Float) {
        rotation = Quat(Vec3(pitch, yaw, roll)).normalize_()
    }

    fun getForward(): Vec3 {
        return rotation.conjugate().times(Vec3(0f, 0f, -1f))
    }

    fun getRight(): Vec3 {
        return rotation.conjugate().times(Vec3(1f, 0f, 0f))
    }

    fun getUp(): Vec3 {
        return rotation.conjugate().times(Vec3(0f, 1f, 0f))
    }
}

