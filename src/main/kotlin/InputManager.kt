import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWCursorPosCallback
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.system.MemoryStack

class InputManager(
    private val window: Long,
    private val cameraCallback: ICameraInputCallback?
) {
    private val TAG: String = this::class.java.name
    private val cameraKeys = arrayOf(GLFW_KEY_W, GLFW_KEY_S, GLFW_KEY_A, GLFW_KEY_D, GLFW_KEY_LEFT_SHIFT, GLFW_KEY_SPACE)

    private var keyCb: GLFWKeyCallback? = null
    private var cursorPosCb: GLFWCursorPosCallback? = null

    private var lastCursorX: Double = 0.0
    private var lastCursorY: Double = 0.0

    private var previousTime: Double = 0.0

    init {
        keyCb = glfwSetKeyCallback(window, ::keyCallback)
        cursorPosCb = glfwSetCursorPosCallback(window, ::cursorPosCallback)

        MemoryStack.stackPush().use { stack ->
            val tmpX = stack.mallocDouble(1)
            val tmpY = stack.mallocDouble(1)
            glfwGetCursorPos(window, tmpX, tmpY)
            lastCursorX = tmpX.get()
            lastCursorY = tmpY.get()
        }

        previousTime = glfwGetTime()
    }

    private fun keyCallback(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
        if (key == GLFW_KEY_Q && action == GLFW_PRESS) {
            glfwSetWindowShouldClose(window, true)
        }
    }

    private fun cursorPosCallback(window: Long, xPos: Double, yPos: Double) {
        val deltaX = (xPos - lastCursorX).toInt()
        val deltaY = (lastCursorY - yPos).toInt()

        cameraCallback?.cursorMoved(deltaX, deltaY)

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
                    cameraCallback.keyPressed(k, currentTime - previousTime)
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
