import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWCursorPosCallback
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.glfw.GLFWMouseButtonCallback

class InputManager : KoinComponent {
    companion object {
        private val TAG: String = this::class.java.name
    }

    private val cameraKeys =
        arrayOf(
            GLFW_KEY_W, GLFW_KEY_S, GLFW_KEY_A, GLFW_KEY_D,
            GLFW_KEY_LEFT_SHIFT, GLFW_KEY_SPACE,
            GLFW_KEY_Q, GLFW_KEY_E
        )

    private val camera by inject<Camera>()

    private var window: Long? = null

    private var cameraCallback: ICameraInputCallback = camera.inputCallback
    private var keyCallback: GLFWKeyCallback? = null
    private var cursorPosCallback: GLFWCursorPosCallback? = null
    private var cursorBtnCallback: GLFWMouseButtonCallback? = null

    private var lastCursorX: Double = 0.0
    private var lastCursorY: Double = 0.0
    private var previousTime: Double = 0.0

    fun attachWindow(window: Long) {
        this.window = window

        keyCallback = glfwSetKeyCallback(window, ::keyCallback)
        cursorPosCallback = glfwSetCursorPosCallback(window, ::cursorPosCallback)
        cursorBtnCallback = glfwSetMouseButtonCallback(window, ::mouseButtonCallback)
        previousTime = glfwGetTime()
        glfwPollEvents()
    }

    private fun keyCallback(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
            glfwSetWindowShouldClose(window, true)
        }
        if (key == GLFW_KEY_G && action == GLFW_PRESS) {
            Engine.gravity = !Engine.gravity
        }
    }

    private fun cursorPosCallback(window: Long, xPos: Double, yPos: Double) {
        val deltaX = (xPos - lastCursorX).toInt()
        val deltaY = (lastCursorY - yPos).toInt()

        cameraCallback.cursorMoved(deltaX, deltaY)

        lastCursorX = xPos
        lastCursorY = yPos
    }

    private fun mouseButtonCallback(window: Long, button: Int, action: Int, mods: Int) {
        if (action == GLFW_PRESS) {
            cameraCallback.mouseButtonPressed(button)
        }
    }

    fun update() {
        glfwPollEvents()

        val currentTime = glfwGetTime() // delta seconds

        // Camera keys
        for (k in cameraKeys) {
            if (glfwGetKey(window!!, k) == GLFW_PRESS) {
                cameraCallback.keyPressed(k, currentTime - previousTime)
            }
        }

        previousTime = currentTime
    }

    fun cleanup() {
        keyCallback?.free()
        cursorPosCallback?.free()
    }
}
