import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import org.lwjgl.Version.getVersion
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL33.*
import org.lwjgl.system.MemoryUtil.*
import java.nio.FloatBuffer
import java.nio.IntBuffer

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
        // X    Y   Z   |   S   T
        // front
        -0.5f,  -0.5f,  0.0f,   0.0f,   0.0f,   // BL 0
        0.5f,   -0.5f,  0.0f,   1.0f,   0.0f,   // BR 1
        0.5f,   0.5f,   0.0f,   1.0f,   1.0f,   // TR 2
        -0.5f,  0.5f,   0.0f,   0.0f,   1.0f,   // TL 3

        // back
        -0.5f,  -0.5f,  1.0f,   0.0f,   0.0f,   // BL 4
        0.5f,   -0.5f,  1.0f,   1.0f,   0.0f,   // BR 5
        0.5f,   0.5f,   1.0f,   1.0f,   1.0f,   // TR 5
        -0.5f,  0.5f,   1.0f,   0.0f,   1.0f,   // TL 6
    )

    val indices = intArrayOf(
        2,3,0,  0,1,2,
        6,7,4,  4,5,6,
        2,1,5,  2,5,6,
        0,1,4,  4,1,5,
        7,3,2,  7,2,6,
        3,4,0,  7,4,3
    )

    // Create floats buffer and fill with vertices
    val verticesBuffer: FloatBuffer = memAllocFloat(vertices.size)
    verticesBuffer
        .put(vertices)
        .flip() // flip resets position to 0

    val indicesBuffer: IntBuffer = memAllocInt(indices.size)
    indicesBuffer
        .put(indices)
        .flip()

    val vao = glGenVertexArrays()
    val vbo = glGenBuffers()
    val ebo = glGenBuffers()

    glBindVertexArray(vao)

    glBindBuffer(GL_ARRAY_BUFFER, vbo)
    glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW)
    memFree(verticesBuffer)

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW)
    memFree(indicesBuffer)

    glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * 4, 0)
    glEnableVertexAttribArray(0)
    glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * 4, 3 * 4)
    glEnableVertexAttribArray(1)

    val texture = Texture()
    texture.createTexture("src/main/resources/Textures/cat.png")

    glBindBuffer(GL_ARRAY_BUFFER, 0) // Unbind VBO
    glBindVertexArray(0) // Unbind VAO

    shaderProgram.use()

    glEnable(GL_DEPTH_TEST)

    while (!glfwWindowShouldClose(window!!)) {
        glClearColor(.2f, .7f, .7f, 0.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // MVP
        val proj = glm.perspective(glm.radians((60f)), 800f / 600f, 0.1f, 100.0f)

        val model = Mat4(1.0f)
            .rotate_(glm.radians(-40f), Vec3(1f, 0f, 0f))
            .rotate_((glfwGetTime().toFloat()), Vec3(1f, 0f, 1f))

        val view = Mat4(1.0f)
            .translate_(Vec3(0f, 0f, -5f))

        val mvp = proj.times_(view).times_(model)
        shaderProgram.setUniformMat4f("mvp", mvp)

        // Render
        glBindVertexArray(vao)
        texture.bind()
        glDrawElements(GL_TRIANGLES, indices.size, GL_UNSIGNED_INT, 0)

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
