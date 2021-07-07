import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWCursorPosCallback
import org.lwjgl.glfw.GLFWKeyCallback

class InputManager(
    private val window: Long,
) {
    private val TAG: String = this::class.java.name
    private val cameraKeys =
        arrayOf(
            GLFW_KEY_W, GLFW_KEY_S, GLFW_KEY_A, GLFW_KEY_D,
            GLFW_KEY_LEFT_SHIFT, GLFW_KEY_SPACE,
            GLFW_KEY_Q, GLFW_KEY_E
        )

    private var cameraCallback: ICameraInputCallback? = null
    private var keyCb: GLFWKeyCallback? = null
    private var cursorPosCb: GLFWCursorPosCallback? = null

    private var lastCursorX: Double = 0.0
    private var lastCursorY: Double = 0.0

    private var previousTime: Double = 0.0

    init {
        keyCb = glfwSetKeyCallback(window, ::keyCallback)
        cursorPosCb = glfwSetCursorPosCallback(window, ::cursorPosCallback)
        previousTime = glfwGetTime()
        glfwPollEvents()
    }

    fun addCamera(camera: Camera) {
        this.cameraCallback = camera.iCameraInput
    }

    private fun keyCallback(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
            glfwSetWindowShouldClose(window, true)
        }
    }

    private fun cursorPosCallback(window: Long, xPos: Double, yPos: Double) {

        cameraCallback?.let {
            val deltaX = (xPos - lastCursorX).toInt()
            val deltaY = (lastCursorY - yPos).toInt()

            it.cursorMoved(deltaX, deltaY)
        }

        lastCursorX = xPos
        lastCursorY = yPos
    }

    fun update() {
        glfwPollEvents()

        val currentTime = glfwGetTime() // delta seconds

        // Camera keys
        cameraCallback?.let {
            for (k in cameraKeys) {
                if (glfwGetKey(window, k) == GLFW_PRESS) {
                    it.keyPressed(k, currentTime - previousTime)
                }
            }
        }

        previousTime = currentTime
    }

    fun cleanup() {
        keyCb?.free()
        cursorPosCb?.free()
    }
}
