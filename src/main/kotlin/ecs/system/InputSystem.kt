package ecs.system

import ecs.component.CameraComponent
import ecs.node.CameraNode
import glm_.func.common.clamp
import glm_.glm
import glm_.vec3.Vec3
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWCursorPosCallback
import org.lwjgl.glfw.GLFWKeyCallback
import utils.Debug
import utils.OpenGLUtils

object InputSystem : BaseSystem() {
    private val TAG: String = this::class.java.name

    private const val MOUSE_SENSITIVITY = 0.1f

    var cameraNodes = mutableListOf<CameraNode>()

    private var window: Long = -1
    private var isAttachedToWindow = false
    private var isFirstCursorMove = true

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

    init {
        glfwPollEvents()
    }

    fun attachToWindow(window: Long) {
        glfwSetInputMode(window, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE)
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
        InputSystem.window = window
        isAttachedToWindow = true
        keyCb = glfwSetKeyCallback(window, InputSystem::keyCallback)
        cursorPosCb = glfwSetCursorPosCallback(window, InputSystem::cursorPosCallback)
        // this.cursorBtnCb = glfwSetMouseButtonCallback(window, ::mouseButtonCallback)
    }

    private fun keyCallback(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
        when {
            key == GLFW_KEY_ESCAPE && action == GLFW_PRESS -> {
                if (isAttachedToWindow) {
                    glfwSetWindowShouldClose(window, true)
                }
            }
            key == GLFW_KEY_B && action == GLFW_PRESS -> {
                RenderSystem.drawBoundingBoxes = !RenderSystem.drawBoundingBoxes
            }
            key == GLFW_KEY_N && action == GLFW_PRESS -> {
                RenderSystem.drawTerrainNormals = !RenderSystem.drawTerrainNormals
            }
            key == GLFW_KEY_F && action == GLFW_PRESS -> {
                DynamicFovSystem.isDynamicFovEnabled = !DynamicFovSystem.isDynamicFovEnabled
            }
        }
    }

    private fun cursorPosCallback(window: Long, xPos: Double, yPos: Double) {
        if (isFirstCursorMove) {
            lastCursorX = xPos
            lastCursorY = yPos
            isFirstCursorMove = false
        }

        currentCursorX = xPos
        currentCursorY = yPos
    }

    override fun update(deltaTime: Float) {
        if (!isStarted) return

        if (!isAttachedToWindow) {
            Debug.loge(TAG, "Not attached to window!")
            return
        }

//        Debug.logd(TAG, "update (deltaTime=$deltaTime)")

        glfwPollEvents()

        for (cameraNode in cameraNodes) {
            if (cameraNode.cameraComponent.isActive) {
                if (!cameraNode.cameraComponent.isInitialized) {
                    initializeCamera(cameraNode)
                }
                updateCamera(cameraNode, deltaTime)
            }
        }

        lastCursorX = currentCursorX
        lastCursorY = currentCursorY
    }

    private fun initializeCamera(cameraNode: CameraNode) {
        val windowSize = OpenGLUtils.getWindowSize(window)
        val aspectRatio = windowSize.x / windowSize.y

        with(cameraNode) {
            cameraComponent.projectionMat = glm.perspective(
                fovy = glm.radians(CameraComponent.FOV_DEG),
                aspect = aspectRatio,
                zNear = CameraComponent.Z_NEAR,
                zFar = CameraComponent.Z_FAR
            )

            transformComponent.rotatable.rotation = cameraComponent.calculateOrientation()
        }
    }

    private fun updateCamera(cameraNode: CameraNode, deltaTime: Float) {
        val deltaX = (currentCursorX - lastCursorX).toInt()
        val deltaY = (lastCursorY - currentCursorY).toInt()

        with(cameraNode) {
            if (deltaX != 0 || deltaY != 0) {
                cameraComponent.yaw += deltaX * MOUSE_SENSITIVITY
                cameraComponent.pitch -= deltaY * MOUSE_SENSITIVITY
                cameraComponent.pitch = cameraComponent.pitch.clamp(-90f, 90f)
                cameraComponent.yaw %= 360
                transformComponent.rotatable.rotation = cameraComponent.calculateOrientation()
            }

            for (key in cameraKeys) {
                when (glfwGetKey(window, key)) {
                    GLFW_PRESS -> {
                        when (key) {
                            // W, S, A, D - moving in x and z-axis
                            GLFW_KEY_W -> {
                                cameraComponent.isMovingForward = true
                                transformComponent.movable.moveBy(
                                    transformComponent.rotatable.getForward() * cameraComponent.cameraSpeed * deltaTime
                                )
                            }
                            GLFW_KEY_S -> {
                                transformComponent.movable.moveBy(
                                    -transformComponent.rotatable.getForward() * cameraComponent.cameraSpeed * deltaTime
                                )
                            }
                            GLFW_KEY_A -> {
                                transformComponent.movable.moveBy(
                                    -transformComponent.rotatable.getRight() * cameraComponent.cameraSpeed * deltaTime
                                )
                            }
                            GLFW_KEY_D -> {
                                transformComponent.movable.moveBy(
                                    transformComponent.rotatable.getRight() * cameraComponent.cameraSpeed * deltaTime
                                )
                            }
                            // Shift, Space - moving in y-axis
                            GLFW_KEY_LEFT_SHIFT -> {
                                transformComponent.movable.moveBy(
                                    Vec3(0f, -1f, 0) * cameraComponent.cameraSpeed * deltaTime
                                )
                            }
                            GLFW_KEY_SPACE -> {
                                transformComponent.movable.moveBy(
                                    Vec3(0f, 1f, 0) * cameraComponent.cameraSpeed * deltaTime
                                )
                            }

                            // Q, E - decreasing/increasing camera speed
                            GLFW_KEY_Q -> {
                                if (cameraComponent.cameraSpeed > CameraComponent.CAMERA_SPEED_MIN) {
                                    cameraComponent.cameraSpeed -= CameraComponent.CAMERA_SPEED_CHANGE_STEP * deltaTime
                                }
                            }
                            GLFW_KEY_E -> {
                                if (cameraComponent.cameraSpeed < CameraComponent.CAMERA_SPEED_MAX) {
                                    cameraComponent.cameraSpeed += CameraComponent.CAMERA_SPEED_CHANGE_STEP * deltaTime
                                }
                            }
                        }
                    }
                    GLFW_RELEASE -> {
                        when (key) {
                            GLFW_KEY_W -> {
                                cameraComponent.isMovingForward = false
                            }
                        }
                    }
                }
            }
        }
    }
}