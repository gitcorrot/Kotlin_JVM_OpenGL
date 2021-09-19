//import org.lwjgl.glfw.GLFW.*
//import org.lwjgl.glfw.GLFWCursorPosCallback
//import org.lwjgl.glfw.GLFWKeyCallback
//import org.lwjgl.glfw.GLFWMouseButtonCallback
//
//class InputManager(
//    private val window: Long,
//) {
//    companion object {
//        private val TAG: String = this::class.java.name
//    }
//
//    private val cameraKeys =
//        arrayOf(
//            GLFW_KEY_W, GLFW_KEY_S, GLFW_KEY_A, GLFW_KEY_D,
//            GLFW_KEY_LEFT_SHIFT, GLFW_KEY_SPACE,
//            GLFW_KEY_Q, GLFW_KEY_E
//        )
//
//    private var cameraCallback: ICameraInputCallback? = null
//    private var keyCb: GLFWKeyCallback?
//    private var cursorPosCb: GLFWCursorPosCallback?
////    private var cursorBtnCb: GLFWMouseButtonCallback?
//
//    private var lastCursorX: Double = 0.0
//    private var lastCursorY: Double = 0.0
//    private var previousTime: Double = 0.0
//
//    init {
//        keyCb = glfwSetKeyCallback(window, ::keyCallback)
//        cursorPosCb = glfwSetCursorPosCallback(window, ::cursorPosCallback)
////        cursorBtnCb = glfwSetMouseButtonCallback(window, ::mouseButtonCallback)
//        previousTime = glfwGetTime()
//        glfwPollEvents()
//    }
//
//    fun setCamera(camera: Camera) {
//        this.cameraCallback = camera.iCameraInput
//    }
//
//    private fun keyCallback(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
//        if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
//            glfwSetWindowShouldClose(window, true)
//        }
////        if (key == GLFW_KEY_G && action == GLFW_PRESS) {
////            Engine.gravity = !Engine.gravity
////        }
//    }
//
//    private fun cursorPosCallback(window: Long, xPos: Double, yPos: Double) {
//
//        cameraCallback?.let {
//            val deltaX = (xPos - lastCursorX).toInt()
//            val deltaY = (lastCursorY - yPos).toInt()
//
//            it.cursorMoved(deltaX, deltaY)
//        }
//
//        lastCursorX = xPos
//        lastCursorY = yPos
//    }
//
////    private fun mouseButtonCallback(window: Long, button: Int, action: Int, mods: Int) {
////        if (action == GLFW_PRESS) {
////            cameraCallback?.mouseButtonPressed(GLFW_MOUSE_BUTTON_RIGHT)
////        }
////    }
//
//    fun update() {
//        glfwPollEvents()
//
//        val currentTime = glfwGetTime() // delta seconds
//
//        // Camera keys
//        cameraCallback?.let {
//            for (k in cameraKeys) {
//                if (glfwGetKey(window, k) == GLFW_PRESS) {
//                    it.keyPressed(k, currentTime - previousTime)
//                }
//            }
//        }
//
//        previousTime = currentTime
//    }
//
//    fun cleanup() {
//        keyCb?.free()
//        cursorPosCb?.free()
//    }
//}
