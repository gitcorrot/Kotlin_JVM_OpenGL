import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import org.lwjgl.glfw.GLFW.*


interface ICameraInputCallback {
    fun keyPressed(key: Int, deltaTime: Double)
    fun cursorMoved(deltaX: Int, deltaY: Int)
}

private const val CAMERA_SPEED = 20.0f
private const val MOUSE_SENSITIVITY = 0.15f

class Camera {
    private val TAG: String = this::class.java.name

    var yaw = -90f
    var pitch = 0f

    private val globalUp = Vec3(0f, 1f, 0f)

    private var position = Vec3(0f, 10f, 25f)
    private var front = Vec3()
    private var right = Vec3()
    private var up = Vec3()

    private fun updateVectors() {
        front.x = glm.cos(glm.radians(yaw)) * glm.cos(glm.radians(pitch))
        front.y = glm.sin(glm.radians(pitch))
        front.z = glm.sin(glm.radians(yaw)) * glm.cos(glm.radians(pitch))
        front.normalizeAssign()

        right = glm.normalize(glm.cross(front, globalUp))
        up = glm.normalize(glm.cross(right, front))
    }

    init {
        updateVectors()
    }

    val viewMat: Mat4
        get() {
            return glm.lookAt(
                position,               // eye -> camera position,
                position.plus(front),   // center -> position we are looking at -> front view of camera
                globalUp                // up -> (0, 1, 0) -> world upright
            )
        }

    val iCameraInput = object : ICameraInputCallback {
        override fun keyPressed(key: Int, deltaTime: Double) {
            when (key) {
                GLFW_KEY_W -> {
                    position.plusAssign(front.times(CAMERA_SPEED * deltaTime))
                }
                GLFW_KEY_S -> {
                    position.minusAssign(front.times(CAMERA_SPEED * deltaTime))
                }
                GLFW_KEY_A -> {
                    position.minusAssign(right.times(CAMERA_SPEED * deltaTime))
                }
                GLFW_KEY_D -> {
                    position.plusAssign(right.times(CAMERA_SPEED * deltaTime))
                }
                GLFW_KEY_LEFT_SHIFT -> {
                    position.minusAssign(globalUp.times(CAMERA_SPEED * deltaTime))
                }
                GLFW_KEY_SPACE -> {
                    position.plusAssign(globalUp.times(CAMERA_SPEED * deltaTime))
                }
            }
//            Debug.logd(TAG, "CAMERA POSITION: \n$position")
        }

        override fun cursorMoved(deltaX: Int, deltaY: Int) {
            yaw += deltaX * MOUSE_SENSITIVITY
            pitch += deltaY * MOUSE_SENSITIVITY

            if (pitch > 89f) pitch = 89f
            if (pitch < -89f) pitch = -89f

//            Debug.logd(TAG, "Yaw: $yaw, Pitch: $pitch")

            updateVectors()
        }
    }
}
