package ecs.system

import ecs.component.CameraComponent
import ecs.node.CameraNode
import glm_.glm
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.glfwSetInputMode
import utils.Debug
import utils.OpenGLUtils
import kotlin.math.max
import kotlin.math.min

object DynamicFovSystem : BaseSystem() {
    private val TAG: String = this::class.java.name

    private const val MAX_FOV_CHANGE = 10f

    var cameraNodes = mutableListOf<CameraNode>()

    private var window: Long = -1
    private var isAttachedToWindow = false

    var isDynamicFovEnabled = true
    var fovChange = 0f

    fun attachToWindow(window: Long) {
        glfwSetInputMode(window, GLFW.GLFW_RAW_MOUSE_MOTION, GLFW.GLFW_TRUE)
        glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED)
        this.window = window
        isAttachedToWindow = true
    }

    override fun update(deltaTime: Float) {
        if (!isStarted) return

        if (!isAttachedToWindow) {
            Debug.loge(TAG, "Not attached to window!")
            return
        }

        for (cameraNode in cameraNodes) {
            if (cameraNode.cameraComponent.isActive) {
                updateDynamicFov(cameraNode.cameraComponent, deltaTime)
            }
        }
    }

    private fun updateDynamicFov(cameraComponent: CameraComponent, deltaTime: Float) {
        if (isDynamicFovEnabled) {
            val windowSize = OpenGLUtils.getWindowSize(window)
            val aspectRatio = windowSize.x / windowSize.y
            fovChange = if (cameraComponent.isMovingForward) {
                min(fovChange + 0.001f * deltaTime, 1f)
            } else {
                max(fovChange - 0.003f * deltaTime, 0f)
            }
            cameraComponent.projectionMat = glm.perspective(
                fovy = glm.radians(CameraComponent.FOV_DEG + (MAX_FOV_CHANGE * fovChange)),
                aspect = aspectRatio,
                zNear = CameraComponent.Z_NEAR,
                zFar = CameraComponent.Z_FAR
            )
        } else {
            fovChange = 0f
            val windowSize = OpenGLUtils.getWindowSize(window)
            val aspectRatio = windowSize.x / windowSize.y
            cameraComponent.projectionMat = glm.perspective(
                fovy = glm.radians(CameraComponent.FOV_DEG),
                aspect = aspectRatio,
                zNear = CameraComponent.Z_NEAR,
                zFar = CameraComponent.Z_FAR
            )
        }
    }
}