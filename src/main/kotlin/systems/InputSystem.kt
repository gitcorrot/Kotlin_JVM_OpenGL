package systems

import nodes.CameraNode
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWCursorPosCallback
import org.lwjgl.glfw.GLFWKeyCallback
import systems.core.BaseSystem
import utils.Debug

object InputSystem : BaseSystem() {
    val TAG: String = this::class.java.name

    var cameraNodes = mutableListOf<CameraNode>()

    private var window: Long = -1
    private var isAttachedToWindow = false

    private val cameraKeys = arrayOf(
        GLFW_KEY_W, GLFW_KEY_S, GLFW_KEY_A, GLFW_KEY_D,
        GLFW_KEY_LEFT_SHIFT, GLFW_KEY_SPACE,
        GLFW_KEY_Q, GLFW_KEY_E
    )

    private var keyCb: GLFWKeyCallback? = null
    private var cursorPosCb: GLFWCursorPosCallback? = null
    // private var cursorBtnCb: GLFWMouseButtonCallback? = null

    private var lastCursorX: Double = 0.0
    private var lastCursorY: Double = 0.0
    private var currentCursorX: Double = 0.0
    private var currentCursorY: Double = 0.0
    private var previousTime: Double = glfwGetTime()

    init {
        glfwPollEvents()
    }

    fun attachToWindow(window: Long) {
        this.window = window
        isAttachedToWindow = true
        this.keyCb = glfwSetKeyCallback(window, ::keyCallback)
        this.cursorPosCb = glfwSetCursorPosCallback(window, ::cursorPosCallback)
        // this.cursorBtnCb = glfwSetMouseButtonCallback(window, ::mouseButtonCallback)
    }

    private fun keyCallback(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
            if (isAttachedToWindow) {
                glfwSetWindowShouldClose(window, true)
            }
        }
    }

    private fun cursorPosCallback(window: Long, xPos: Double, yPos: Double) {
        currentCursorX = xPos
        currentCursorY = yPos
    }

    override fun update(deltaTime: Float) {
        if (!isStarted) return
        Debug.logd(TAG, "update (deltaTime=$deltaTime)")

        glfwPollEvents()

        for (cameraNode in cameraNodes) {
            val deltaX = (currentCursorX - lastCursorX).toInt()
            val deltaY = (lastCursorY - currentCursorY).toInt()
            if (deltaX != 0 || deltaY != 0) {
                cameraNode.cameraComponent.camera.cursorMoved(deltaX, deltaY)
            }

            for (k in cameraKeys) {
                if (glfwGetKey(window, k) == GLFW_PRESS) {
                    cameraNode.cameraComponent.camera.keyPressed(k, deltaTime)
                }
            }
        }

        lastCursorX = currentCursorX
        lastCursorY = currentCursorY
    }
}