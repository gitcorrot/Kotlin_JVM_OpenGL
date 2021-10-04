package systems

import components.CameraComponent
import glm_.func.common.clamp
import glm_.vec3.Vec3
import nodes.CameraNode
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWCursorPosCallback
import org.lwjgl.glfw.GLFWKeyCallback
import systems.core.BaseSystem
import utils.Debug

object InputSystem : BaseSystem() {
    private const val MOUSE_SENSITIVITY = 0.1f

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
            if (cameraNode.cameraComponent.isActive) {
                updateCamera(cameraNode, deltaTime)
            }
        }

        lastCursorX = currentCursorX
        lastCursorY = currentCursorY
    }

    private fun updateCamera(cameraNode: CameraNode, deltaTime: Float) {
        val deltaX = (currentCursorX - lastCursorX).toInt()
        val deltaY = (lastCursorY - currentCursorY).toInt()

        with(cameraNode.cameraComponent) {
            if (deltaX != 0 || deltaY != 0) {
                if (firstCursorMoved) {
                    yaw += deltaX * MOUSE_SENSITIVITY
                    pitch -= deltaY * MOUSE_SENSITIVITY
                    pitch = pitch.clamp(-90f, 90f)
                    yaw %= 360
                } else {
                    firstCursorMoved = true
                }

                updateOrientation()
            }

            for (key in cameraKeys) {
                if (glfwGetKey(window, key) == GLFW_PRESS) {
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
                            if (cameraSpeed > CameraComponent.CAMERA_SPEED_MIN) {
                                cameraSpeed -= CameraComponent.CAMERA_SPEED_CHANGE_STEP * deltaTime
                            }
                        }
                        GLFW_KEY_E -> {
                            if (cameraSpeed < CameraComponent.CAMERA_SPEED_MAX) {
                                cameraSpeed += CameraComponent.CAMERA_SPEED_CHANGE_STEP * deltaTime
                            }
                        }
                    }
                }
            }
        }
    }
}