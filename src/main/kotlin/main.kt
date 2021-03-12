import glm_.glm
import glm_.mat4x4.Mat4
import glm_.mat4x4.Mat4x4t
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.Version.getVersion
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL33.*
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.*
import java.nio.FloatBuffer

// https://github.com/Zi0P4tch0/LWJGL-Kotlin-Example/blob/master/src/main/kotlin/com/example/Engine.kt

private var window: Long? = null

private var errCallback: GLFWErrorCallback? = null
private var keyCallback: GLFWKeyCallback? = null

const val TAG = "Main"

fun init() {

    errCallback = glfwSetErrorCallback(GLFWErrorCallback.create { error, description ->
        Debug.loge(TAG, "Error $error: $description")
    })

    if (!glfwInit()) {
        throw IllegalStateException("Unable to initialize GLFW")
    }

    // Configure our window
    glfwDefaultWindowHints()
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)

    // Create the window
    window = glfwCreateWindow(600, 600, "Hello World!", NULL, NULL)
    if (window == NULL) {
        throw RuntimeException("Failed to create the GLFW window")
    }

    // Get the resolution of the primary monitor
    glfwGetVideoMode(glfwGetPrimaryMonitor())?.let {
        // Center our window
        glfwSetWindowPos(
            window!!,
            (it.width() - 600) / 2,
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

    Debug.logi(TAG, "JLWGL Version: ${getVersion()}")
    Debug.logi(TAG, "OpenGL Version: ${glGetString(GL_VERSION)}")
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
        // X    Y   Z   |   R   G   B   |   S   T
        -0.5f, -0.5f, 0.0f, 0.9f, 0.0f, 0.0f, 0.0f, 0.0f,   // BL
        -0.5f, 0.5f, 0.0f, 0.0f, 0.0f, 0.9f, 0.0f, 1.0f,    // TL
        0.5f, 0.5f, 0.0f, 0.0f, 0.0f, 0.9f, 1.0f, 1.0f,     // TR

        0.5f, 0.5f, 0.0f, 0.0f, 0.0f, 0.9f, 1.0f, 1.0f,     // TR
        -0.5f, -0.5f, 0.0f, 0.9f, 0.0f, 0.0f, 0.0f, 0.0f,   // BL
        0.5f, -0.5f, 0.0f, 0.0f, 0.9f, 0.0f, 1.0f, 0.0f,    // BR
    )

    // Create floats buffer and fill with vertices
    val verticesBuffer: FloatBuffer = memAllocFloat(vertices.size)
    verticesBuffer
        .put(vertices)
        .flip() // flip resets position to 0

    val vao = glGenVertexArrays()
    val vbo = glGenBuffers()

    glBindVertexArray(vao)
    glBindBuffer(GL_ARRAY_BUFFER, vbo)
    glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW)
    memFree(verticesBuffer)
    glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * 4, 0)
    glEnableVertexAttribArray(0)
    glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * 4, 3 * 4)
    glEnableVertexAttribArray(1)
    glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * 4, 6 * 4)
    glEnableVertexAttribArray(2)

    val texture = Texture()
    texture.createTexture("src/main/resources/Textures/cat.png")

    glBindBuffer(GL_ARRAY_BUFFER, 0) // Unbind VBO
    glBindVertexArray(0) // Unbind VAO

    shaderProgram.use()

    while (!glfwWindowShouldClose(window!!)) {
        glClearColor(.2f, .7f, .7f, 0.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // Transform
        val rot: Float = glfwGetTime().toFloat()
        val transform = Mat4(1.0f).rotate_(rot, Vec3(0f, 0f, 1f) )
        shaderProgram.setUniformMat4f("transform", transform)

        // Render
        glBindVertexArray(vao)
        texture.bind()
        glDrawArrays(GL_TRIANGLES, 0, 6)

        glfwSwapBuffers(window!!)
        glfwPollEvents()
    }

    // Cleanup
    glDeleteVertexArrays(vao)
    shaderProgram.cleanup()
}

fun readOpenGLError() {
    var e = glGetError()
    // For debugging only
    while (e != GL_NO_ERROR) {
        Debug.loge(TAG, "OpenGL error: $e")
        e = glGetError()
    }
}

fun main() {
    Debug.DEBUG_LEVEL = Debug.DebugLevel.DEBUG

    init()
    loop()

    // Destroy window
    Debug.logi(TAG, "Destroying window...")
    glfwDestroyWindow(window!!)
    keyCallback?.free()

    // Terminate GLFW
    glfwTerminate()
    glfwSetErrorCallback(null)?.free()
    Debug.logi(TAG, "Window destroyed!")
}
