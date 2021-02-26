import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL

// https://github.com/Zi0P4tch0/LWJGL-Kotlin-Example/blob/master/src/main/kotlin/com/example/Engine.kt

private var window: Long? = null

private var errCallback: GLFWErrorCallback? = null
private var keyCallback: GLFWKeyCallback? = null

fun init() {

    // Setup an error callback. The default implementation
    // will print the error message in System.err.
    errCallback = glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err))

    if (!glfwInit()) {
        throw IllegalStateException("Unable to initialize GLFW")
    }

    // Configure our window
    glfwDefaultWindowHints()
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)

    // Create the window
    window = glfwCreateWindow(800, 600, "Hello World!", NULL, NULL)
    if (window == NULL) {
        throw RuntimeException("Failed to create the GLFW window")
    }

    // Get the resolution of the primary monitor
    val vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())

    vidMode?.let {
        // Center our window
        glfwSetWindowPos(
            window!!,
            (it.width() - 800) / 2,
            (it.height() - 600) / 2
        )
    }

    // Setup a key callback. It will be called every time a key is pressed, repeated or released.
    keyCallback = glfwSetKeyCallback(window!!, object : GLFWKeyCallback() {
        override fun invoke(
            window: Long,
            key: Int,
            scancode: Int,
            action: Int,
            mods: Int
        ) {

            if (key == GLFW_KEY_Q && action == GLFW_PRESS) {
                glfwSetWindowShouldClose(window, true)
            }

        }
    })

    // Make the OpenGL context current
    glfwMakeContextCurrent(window!!)

    // Enable v-sync
    glfwSwapInterval(1)

    // Make the window visible
    glfwShowWindow(window!!)

    // This line is critical for LWJGL's interoperation with GLFW's
    // OpenGL context, or any context that is managed externally.
    // LWJGL detects the context that is current in the current thread,
    // creates the GLCapabilities instance and makes the OpenGL
    // bindings available for use.
    GL.createCapabilities()
}

fun loop() {

    // Set the clear color
    glClearColor(.5f, .6f, .7f, 0.0f)

    while (!glfwWindowShouldClose(window!!)) {


        // Clear the framebuffer
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // Swap the color buffers
        glfwSwapBuffers(window!!)

        // Poll for window events. The key callback above will only be
        // invoked during this call.
        glfwPollEvents()
    }

}

fun main() {
    init()
    loop()

    // Destroy window
    println("Destroying window")
    glfwDestroyWindow(window!!)
    keyCallback?.free()

    // Terminate GLFW
    glfwTerminate()
}
