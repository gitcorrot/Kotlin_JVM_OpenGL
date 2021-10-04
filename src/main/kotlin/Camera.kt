import glm_.glm
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.vec3.Vec3
import data.Movable
import data.Rotatable
import org.lwjgl.glfw.GLFW.*

// TODO: make movable and rotatable variables of Camera
class Camera {
    companion object {
        private val TAG: String = this::class.java.name

        private const val MOUSE_SENSITIVITY = 0.1f
        private const val CAMERA_SPEED_MAX = 0.05f
        private const val CAMERA_SPEED_MIN = 0.005f
        private const val CAMERA_SPEED_CHANGE_STEP = 0.00001f
    }

    private var cameraSpeed = 0.01f
    private val movable = Movable(Vec3(0f, 5f, 0f))
    private var rotatable = Rotatable()

    private var firstCursorMoved = false
    var yaw = 45f
    var pitch = 45f

    val viewMat: Mat4
        get() = rotatable.rotation.toMat4().translate(-movable.position)

    init {
        updateOrientation()
    }

    fun keyPressed(key: Int, deltaTime: Float) {
        when (key) {

            // W, S, A, D - moving in x and z-axis
            GLFW_KEY_W -> {
                val dPos = rotatable.getForward() * cameraSpeed * deltaTime
                movable.moveBy(dPos)
            }
            GLFW_KEY_S -> {
                val dPos = -rotatable.getForward() * cameraSpeed * deltaTime
                movable.moveBy(dPos)
            }
            GLFW_KEY_A -> {
                val dPos = -rotatable.getRight() * cameraSpeed * deltaTime
                movable.moveBy(dPos)
            }
            GLFW_KEY_D -> {
                val dPos = rotatable.getRight() * cameraSpeed * deltaTime
                movable.moveBy(dPos)
            }

            // Shift, Space - moving in y-axis
            GLFW_KEY_LEFT_SHIFT -> {
                val dPos = Vec3(0f, -1f, 0) * cameraSpeed * deltaTime
                movable.moveBy(dPos)
            }
            GLFW_KEY_SPACE -> {
                val dPos = Vec3(0f, 1f, 0) * cameraSpeed * deltaTime
                movable.moveBy(dPos)
            }

            // Q, E - decreasing/increasing camera speed
            GLFW_KEY_Q -> {
                if (cameraSpeed > CAMERA_SPEED_MIN) {
                    cameraSpeed -= CAMERA_SPEED_CHANGE_STEP * deltaTime
                }
            }
            GLFW_KEY_E -> {
                if (cameraSpeed < CAMERA_SPEED_MAX) {
                    cameraSpeed += CAMERA_SPEED_CHANGE_STEP * deltaTime
                }
            }
        }
        // Debug.logd(TAG, "CAMERA POSITION: $position")
    }

    fun cursorMoved(deltaX: Int, deltaY: Int) {
        if (firstCursorMoved) {
            yaw += deltaX * MOUSE_SENSITIVITY
            pitch -= deltaY * MOUSE_SENSITIVITY

            if (pitch > 90f) pitch = 90f
            if (pitch < -90f) pitch = -90f

            yaw %= 360
        } else {
            firstCursorMoved = true
        }

        updateOrientation()

        // Debug.logd(TAG, "Yaw: $yaw, Pitch: $pitch, Quat: $rotation")
    }

    private fun updateOrientation() {
        // Convert Euler angles to quaternion
        val pitchQuat = Quat.angleAxis(glm.radians(pitch), Vec3(1, 0, 0))
        val yawQuat = Quat.angleAxis(glm.radians(yaw), Vec3(0, 1, 0))

        rotatable.rotation = pitchQuat.times(yawQuat).normalize()
    }
}
