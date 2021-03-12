import org.lwjgl.Version.getVersion
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.opengl.GL
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
    glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * 4, 0)
    glEnableVertexAttribArray(0)
    glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * 4, 3 * 4)
    glEnableVertexAttribArray(1)
    glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * 4, 6 * 4)
    glEnableVertexAttribArray(2)

    val stack = MemoryStack.stackPush() // stack - we don't need to free it
    val tmpChannels = stack.mallocInt(1)
    val tmpWidth = stack.mallocInt(1)
    val tmpHeight = stack.mallocInt(1)

    STBImage.stbi_set_flip_vertically_on_load(true)
    val image = STBImage.stbi_load(
        "src/main/resources/Textures/cat.png",
        tmpWidth, tmpHeight, tmpChannels, 0
    ) ?: throw Exception("Can't load image! Ensure that image is in proper resources folder.")

    val imageWidth = tmpWidth.get()
    val imageHeight = tmpHeight.get()

    Debug.logi(TAG, "Successfully loaded ${imageWidth}x${imageHeight}px, ${tmpChannels.get()} channels image")

    val textureID = glGenTextures()
    glBindTexture(GL_TEXTURE_2D, textureID)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, imageWidth, imageHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, image)
    glGenerateMipmap(textureID)
    STBImage.stbi_image_free(image)

    glBindBuffer(GL_ARRAY_BUFFER, 0) // Unbind VBO
    glBindVertexArray(0) // Unbind VAO
    memFree(verticesBuffer)

    shaderProgram.use()

    while (!glfwWindowShouldClose(window!!)) {
        glClearColor(.2f, .7f, .7f, 0.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // Render triangle
        glBindVertexArray(vao)
        glBindTexture(GL_TEXTURE_2D, textureID)
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
