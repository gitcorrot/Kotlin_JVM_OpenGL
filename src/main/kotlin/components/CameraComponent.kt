package components

import data.Movable
import data.Rotatable
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.vec3.Vec3
import utils.OpenGLUtils

data class CameraComponent(
    val window: Long
) {
    companion object {
        const val CAMERA_SPEED_MAX = 0.05f
        const val CAMERA_SPEED_MIN = 0.005f
        const val CAMERA_SPEED_CHANGE_STEP = 0.00001f
        const val FOV_DEG = 60f
        const val Z_NEAR = 0.1f
        const val Z_FAR = 1000.0f
    }

    var isActive = true
    var cameraSpeed = 0.01f
    val movable = Movable(Vec3(0f, 5f, 0f))
    var rotatable = Rotatable()
    var yaw = 45f
    var pitch = 45f
    var firstCursorMoved = false
    var projectionMat: Mat4? = null

    val viewMat: Mat4
        get() = rotatable.rotation.toMat4().translate(-movable.position)

    init {
        updateOrientation()
        val windowSize = OpenGLUtils.getWindowSize(window)
        val aspectRatio = windowSize.x / windowSize.y
        projectionMat = glm.perspective(glm.radians(FOV_DEG), aspectRatio, Z_NEAR, Z_FAR)
    }

    fun updateOrientation() {
        // Convert Euler angles to quaternion
        val pitchQuat = Quat.angleAxis(glm.radians(pitch), Vec3(1, 0, 0))
        val yawQuat = Quat.angleAxis(glm.radians(yaw), Vec3(0, 1, 0))

        rotatable.rotation = pitchQuat.times(yawQuat).normalize()
    }
}