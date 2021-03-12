import org.lwjgl.Version.getVersion
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL33.*
import org.lwjgl.system.MemoryUtil.*
import java.nio.FloatBuffer

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
    glfwGetVideoMode(glfwGetPrimaryMonitor())?.let {
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
            if (key == GLFW_KEY_SPACE && action == GLFW_RELEASE) {
                print('*')
            }
        }
    })

    glfwMakeContextCurrent(window!!)
    glfwSwapInterval(1) // Enable v-sync
    glfwShowWindow(window!!)
    GL.createCapabilities()

    println("JLWGL Version: ${getVersion()}")
    println("OpenGL Version: ${glGetString(GL_VERSION)}")
}

fun loop() {

    // Create program with shaders
    val shaderProgram = ShaderProgram()
    val vertexShaderString = ResourcesUtils.loadStringFromFile("Shaders/vertex_shader.glsl")
    val fragmentShaderString = ResourcesUtils.loadStringFromFile("Shaders/fragment_shader.glsl")
    shaderProgram.createShader(vertexShaderString, GL_VERTEX_SHADER)
    shaderProgram.createShader(fragmentShaderString, GL_FRAGMENT_SHADER)
    shaderProgram.link()

    // primitive type array
    val vertices = floatArrayOf(
        -0.5f, -0.5f, 0.0f,
        0.5f, -0.5f, 0.0f,
        0.0f, 0.5f, 0.0f,
    )

    // Create floats buffer and fill with vertices
    val verticesBuffer: FloatBuffer = memAllocFloat(vertices.size)
    verticesBuffer
        .put(vertices)
        .flip() // flip resets position to 0

    // VAO and VBO
    val vao = glGenVertexArrays()
    val vbo = glGenBuffers()

    glBindVertexArray(vao)

    glBindBuffer(GL_ARRAY_BUFFER, vbo)
    glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW)

    glVertexAttribPointer(
        0,
        3,
        GL_FLOAT,
        false,
        0, //  If stride is 0, the generic vertex attributes are understood to be tightly packed in the array
        0
    )
    glEnableVertexAttribArray(0)

    glBindBuffer(GL_ARRAY_BUFFER, 0) // Unbind VBO
    glBindVertexArray(0) // Unbind VAO
    memFree(verticesBuffer)

    shaderProgram.use()

    while (!glfwWindowShouldClose(window!!)) {
        glClearColor(.5f, .6f, .7f, 0.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // Render triangle
        glBindVertexArray(vao)
        glDrawArrays(GL_TRIANGLES, 0, 3)

        glfwSwapBuffers(window!!)
        glfwPollEvents()
    }

    // Cleanup
    glDeleteVertexArrays(vao)
    shaderProgram.cleanup()
}

fun main() {
    init()
    loop()

    // Destroy window
    println("\nDestroying window...")
    glfwDestroyWindow(window!!)
    keyCallback?.free()

    // Terminate GLFW
    glfwTerminate()
    glfwSetErrorCallback(null)?.free()
    println("Window destroyed!")
}
