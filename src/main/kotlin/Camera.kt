import glm_.glm
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.vec3.Vec3
import interfaces.Movable
import interfaces.Rotatable
import org.lwjgl.glfw.GLFW.*
import utils.Debug


interface ICameraInputCallback {
    fun keyPressed(key: Int, deltaTime: Double)
    fun cursorMoved(deltaX: Int, deltaY: Int)
    fun mouseButtonPressed(mouseButton: Int)
}

private const val MOUSE_SENSITIVITY = 0.1f
private const val CAMERA_SPEED_CHANGE_STEP = 25f
private const val CAMERA_SPEED_MAX = 50f
private const val CAMERA_SPEED_MIN = 1f

class Camera : Movable, Rotatable {
    companion object {
        private val TAG: String = this::class.java.name
    }

    private var cameraSpeed = 20.0f
    override val position = Vec3(-15f, 30, 15f)
    override var rotation: Quat = Quat()

    var yaw = 45f
    var pitch = 45f

    val viewMat: Mat4
        get() = rotation.toMat4().translate(-position)

    init {
        updateOrientation()
    }


    val inputCallback = object : ICameraInputCallback {
        override fun keyPressed(key: Int, deltaTime: Double) {
            when (key) {

                // W, S, A, D - moving in x and z-axis
                GLFW_KEY_W -> {
                    val dPos = getForward() * cameraSpeed * deltaTime
                    moveBy(dPos)
                }
                GLFW_KEY_S -> {
                    val dPos = -getForward() * cameraSpeed * deltaTime
                    moveBy(dPos)
                }
                GLFW_KEY_A -> {
                    val dPos = -getRight() * cameraSpeed * deltaTime
                    moveBy(dPos)
                }
                GLFW_KEY_D -> {
                    val dPos = getRight() * cameraSpeed * deltaTime
                    moveBy(dPos)
                }

                // Shift, Space - moving in y-axis
                GLFW_KEY_LEFT_SHIFT -> {
                    val dPos = Vec3(0f, -1f, 0) * cameraSpeed * deltaTime
                    moveBy(dPos)
                }
                GLFW_KEY_SPACE -> {
                    val dPos = Vec3(0f, 1f, 0) * cameraSpeed * deltaTime
                    moveBy(dPos)
                }

                // Q, E - decreasing/increasing camera speed
                GLFW_KEY_Q -> {
                    if (cameraSpeed > CAMERA_SPEED_MIN) {
//                        Debug.logd(TAG, (CAMERA_SPEED_CHANGE_STEP * deltaTime.toFloat()).toString())
                        cameraSpeed -= CAMERA_SPEED_CHANGE_STEP * deltaTime.toFloat()
                    }
                }
                GLFW_KEY_E -> {
                    if (cameraSpeed < CAMERA_SPEED_MAX) {
                        cameraSpeed += CAMERA_SPEED_CHANGE_STEP * deltaTime.toFloat()
                    }
                }
            }
            // Debug.logd(TAG, "CAMERA POSITION: $position")
        }

        override fun cursorMoved(deltaX: Int, deltaY: Int) {
            yaw += deltaX * MOUSE_SENSITIVITY
            pitch -= deltaY * MOUSE_SENSITIVITY

            if (pitch > 90f) pitch = 90f
            if (pitch < -90f) pitch = -90f

            yaw %= 360

            updateOrientation()

            // Debug.logd(TAG, "Yaw: $yaw, Pitch: $pitch, Quat: $rotation")
        }

        override fun mouseButtonPressed(mouseButton: Int) {
            Debug.logd(TAG, "Mouse button pressed ($mouseButton)")
        }
    }

    private fun updateOrientation() {
        // Convert Euler angles to quaternion
        val pitchQuat = Quat.angleAxis(glm.radians(pitch), Vec3(1, 0, 0))
        val yawQuat = Quat.angleAxis(glm.radians(yaw), Vec3(0, 1, 0))

        rotation = pitchQuat.times(yawQuat).normalize()
    }
}
