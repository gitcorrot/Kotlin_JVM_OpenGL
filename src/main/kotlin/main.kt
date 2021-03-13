import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import org.lwjgl.Version.getVersion
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL33.*
import org.lwjgl.system.MemoryUtil.NULL

const val TAG = "Main"

const val WINDOW_WIDTH = 800
const val WINDOW_HEIGHT = 600

fun createWindow(): Long {
    glfwDefaultWindowHints()
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)

    // Create the window
    val window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Kotlin OpenGL", NULL, NULL)
    if (window == NULL) {
        throw RuntimeException("Failed to create the GLFW window")
    }

    return window
}

fun main() {
    Debug.DEBUG_LEVEL = Debug.DebugLevel.DEBUG

    val errCallback = glfwSetErrorCallback(GLFWErrorCallback.create { error, description ->
        Debug.loge(TAG, "Error $error: $description")
    })

    if (!glfwInit()) {
        throw IllegalStateException("Unable to initialize GLFW")
    }

    val window = createWindow()

    // Setup a key callback. It will be called every time a key is pressed, repeated or released.
    val keyCallback = glfwSetKeyCallback(window, object : GLFWKeyCallback() {
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

    glfwMakeContextCurrent(window)
    glfwSwapInterval(1) // Enable v-sync
    GL.createCapabilities()

    Debug.logi(TAG, "JLWGL Version: ${getVersion()}")
    Debug.logi(TAG, "OpenGL Version: ${glGetString(GL_VERSION)}")

    // primitive type array
    val vertices = floatArrayOf(
        // X    Y   Z   |   S   T
        // front
        -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,   // BL 0
        0.5f, -0.5f, -0.5f, 1.0f, 0.0f,   // BR 1
        0.5f, 0.5f, -0.5f, 1.0f, 1.0f,   // TR 2
        -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,   // TL 3

        // back
        -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,   // BL 4
        0.5f, -0.5f, 0.5f, 1.0f, 0.0f,   // BR 5
        0.5f, 0.5f, 0.5f, 1.0f, 1.0f,   // TR 5
        -0.5f, 0.5f, 0.5f, 0.0f, 1.0f,   // TL 6
    )

    val indices = intArrayOf(
        2, 3, 0, 0, 1, 2,
        6, 7, 4, 4, 5, 6,
        2, 1, 5, 2, 5, 6,
        0, 1, 4, 4, 1, 5,
        7, 3, 2, 7, 2, 6,
        3, 4, 0, 7, 4, 3
    )

    val shaderProgram = ShaderProgram()
    val vertexShaderString = ResourcesUtils.loadStringFromFile("Shaders/vertex_shader.glsl")
    val fragmentShaderString = ResourcesUtils.loadStringFromFile("Shaders/fragment_shader.glsl")
    shaderProgram.createShader(vertexShaderString, GL_VERTEX_SHADER)
    shaderProgram.createShader(fragmentShaderString, GL_FRAGMENT_SHADER)
    shaderProgram.link()
    shaderProgram.use()

    val model = Model()
    model.create(vertices, indices)
    model.addTexture("src/main/resources/Textures/cat.png")

    val renderer = Renderer(window, WINDOW_WIDTH, WINDOW_HEIGHT)

    glEnable(GL_DEPTH_TEST)

    while (!glfwWindowShouldClose(window)) {

        // TODO: Implement camera
        val viewMat = Mat4(1.0f)
            .translate_(Vec3(0f, 0f, -5f))

        shaderProgram.setUniformMat4f(
            "mvp",
            Mat4(1f)
                .times_(renderer.projectionMat)
                .times_(viewMat)
                .times_(model.transformation)
        )

        renderer.render(model)
    }

    // Cleanup
    model.cleanup()
    shaderProgram.cleanup()

    // Destroy window
    Debug.logi(TAG, "Destroying window...")
    glfwDestroyWindow(window)
    keyCallback?.free()
    errCallback?.free()

    // Terminate GLFW
    glfwTerminate()
    glfwSetErrorCallback(null)?.free()
    Debug.logi(TAG, "Window destroyed!")
}

fun readOpenGLError() {
    var e = glGetError()
    // For debugging only
    while (e != GL_NO_ERROR) {
        Debug.loge(TAG, "OpenGL error: $e")
        e = glGetError()
    }
}