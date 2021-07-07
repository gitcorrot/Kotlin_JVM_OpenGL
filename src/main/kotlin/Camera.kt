import glm_.glm
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.vec3.Vec3
import interfaces.Movable
import org.lwjgl.glfw.GLFW.*


interface ICameraInputCallback {
    fun keyPressed(key: Int, deltaTime: Double)
    fun cursorMoved(deltaX: Int, deltaY: Int)
}

private const val MOUSE_SENSITIVITY = 0.15f
private const val CAMERA_SPEED_CHANGE_STEP = 25f
private const val CAMERA_SPEED_MAX = 50f
private const val CAMERA_SPEED_MIN = 1f

class Camera : Movable {
    private val TAG: String = this::class.java.name

    override val position = Vec3(-25f, 50, 25f)
    var yaw = 45f
    var pitch = 45f
    var orientation = Quat(Vec3(glm.radians(yaw), glm.radians(pitch), 0f)).normalize()

    private var cameraSpeed = 20.0f

    private fun updateOrientation() {
        // Convert Euler angles to quaternion
        val pitchQuat = Quat.angleAxis(glm.radians(pitch), Vec3(1, 0, 0))
        val yawQuat = Quat.angleAxis(glm.radians(yaw), Vec3(0, 1, 0))
        orientation = pitchQuat.times(yawQuat).normalize()
    }

    init {
        updateOrientation()
    }

    val viewMat: Mat4
        get() {
            val positionMat = Mat4(1f).translate_(-position)
            return orientation.toMat4().times(positionMat)
        }

    val iCameraInput = object : ICameraInputCallback {
        override fun keyPressed(key: Int, deltaTime: Double) {
            when (key) {

                // W, S, A, D - moving in x and z axis
                GLFW_KEY_W -> {
                    val dPos = orientation.conjugate().times(Vec3(0f, 0f, -1f)) * cameraSpeed * deltaTime
                    moveBy(dPos)
                }
                GLFW_KEY_S -> {
                    val dPos = orientation.conjugate().times(Vec3(0f, 0f, 1f)) * cameraSpeed * deltaTime
                    moveBy(dPos)
                }
                GLFW_KEY_A -> {
                    val dPos = orientation.conjugate().times(Vec3(-1f, 0f, 0)) * cameraSpeed * deltaTime
                    moveBy(dPos)
                }
                GLFW_KEY_D -> {
                    val dPos = orientation.conjugate().times(Vec3(1f, 0f, 0)) * cameraSpeed * deltaTime
                    moveBy(dPos)
                }

                // Shift, Space - moving in y axis
                GLFW_KEY_LEFT_SHIFT -> {
                    val dPos = orientation.conjugate().times(Vec3(0f, -1f, 0)) * cameraSpeed * deltaTime
                    moveBy(dPos)
                }
                GLFW_KEY_SPACE -> {
                    val dPos = orientation.conjugate().times(Vec3(0f, 1f, 0)) * cameraSpeed * deltaTime
                    moveBy(dPos)
                }

                // Q, E - decreasing/increasing camera speed
                GLFW_KEY_Q -> {
                    if (cameraSpeed > CAMERA_SPEED_MIN) {
                        utils.Debug.logd(TAG, (CAMERA_SPEED_CHANGE_STEP * deltaTime.toFloat()).toString())
                        cameraSpeed -= CAMERA_SPEED_CHANGE_STEP * deltaTime.toFloat()
                    }
                }
                GLFW_KEY_E -> {
                    if (cameraSpeed < CAMERA_SPEED_MAX) {
                        cameraSpeed += CAMERA_SPEED_CHANGE_STEP * deltaTime.toFloat()
                    }
                }
            }
//            Debug.logd(TAG, "CAMERA POSITION: $position")
        }

        override fun cursorMoved(deltaX: Int, deltaY: Int) {
            yaw += deltaX * MOUSE_SENSITIVITY
            pitch -= deltaY * MOUSE_SENSITIVITY

            if (pitch > 90f) pitch = 90f
            if (pitch < -90f) pitch = -90f

            updateOrientation()

//            Debug.logd(TAG, "Yaw: $yaw, Pitch: $pitch, Quat: $orientation")
        }
    }
}
