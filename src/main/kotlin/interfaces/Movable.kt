package interfaces

import data.Transformation
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3

interface Movable {

    val transformation: Transformation

    val transformationMat: Mat4
        get() {
            return Mat4(1f)
                .translate(transformation.translation)
                .times(transformation.rotation)
                .scale_(transformation.scale)
        }

    fun moveBy(x: Float, y: Float, z: Float) {
        transformation.translation.x += x;
        transformation.translation.y += y;
        transformation.translation.z += z;
    }

    fun moveTo(x: Float, y: Float, z: Float) {
        transformation.translation.x = x;
        transformation.translation.y = y;
        transformation.translation.z = z;
    }

    // TODO: Implement quaternions
    fun rotateBy(yaw: Float, pitch: Float, roll: Float) {
        transformation.rotation
            .rotate_(glm.radians(yaw), Vec3(0f, 1f, 0f))
            .rotate_(glm.radians(pitch), Vec3(1f, 0f, 0f))
            .rotate_(glm.radians(roll), Vec3(0f, 0f, 1f))
    }

    fun rotateTo(yaw: Float, pitch: Float, roll: Float) {
        transformation.rotation = Mat4()
            .rotate_(glm.radians(yaw), Vec3(0f, 1f, 0f))
            .rotate_(glm.radians(pitch), Vec3(1f, 0f, 0f))
            .rotate_(glm.radians(roll), Vec3(0f, 0f, 1f))
    }

    fun scaleBy(x: Float, y: Float, z: Float) {
        transformation.scale.x += x
        transformation.scale.y += y
        transformation.scale.z += z
    }

    fun scaleTo(x: Float, y: Float, z: Float) {
        transformation.scale.x = x
        transformation.scale.y = y
        transformation.scale.z = z
    }

    fun scaleTo(scale: Float) {
        transformation.scale.x = scale
        transformation.scale.y = scale
        transformation.scale.z = scale
    }
}
