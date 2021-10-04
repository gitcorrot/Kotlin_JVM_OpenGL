package components

import glm_.glm
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.vec3.Vec3

data class CameraComponent(
    val window: Long,
    var yaw: Float = 45f,
    var pitch: Float = 45f,
    var cameraSpeed: Float = 0.01f,
    var projectionMat: Mat4? = null,
    var isActive: Boolean = true,
    var isInitialized: Boolean = false
) {
    companion object {
        const val CAMERA_SPEED_MAX = 0.05f
        const val CAMERA_SPEED_MIN = 0.005f
        const val CAMERA_SPEED_CHANGE_STEP = 0.00001f
        const val FOV_DEG = 60f
        const val Z_NEAR = 0.1f
        const val Z_FAR = 1000.0f
    }

    fun calculateOrientation(): Quat {
        // Convert Euler angles to quaternion
        val pitchQuat = Quat.angleAxis(glm.radians(pitch), Vec3(1, 0, 0))
        val yawQuat = Quat.angleAxis(glm.radians(yaw), Vec3(0, 1, 0))

        return pitchQuat.times(yawQuat).normalize()
    }
}