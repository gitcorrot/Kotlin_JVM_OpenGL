import glm_.vec3.Vec3
import org.lwjgl.Version.getVersion
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL33.*
import org.lwjgl.system.MemoryUtil.NULL
import kotlin.random.Random

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

    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
    glfwMakeContextCurrent(window)
    glfwSwapInterval(1) // Enable v-sync
    GL.createCapabilities()

    Debug.logi(TAG, "JLWGL Version: ${getVersion()}")
    Debug.logi(TAG, "OpenGL Version: ${glGetString(GL_VERSION)}")

    // -------------------------------------------------------------------------------------------------------------- //

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

    val plainVertices = floatArrayOf(
        // X    Y   Z   |   S   T
        -25f, -0f, -25f, 0.0f, 0.0f,   // bl
        25f, -0f, -25f, 1.0f, 0.0f,   // br
        25f, -0f, 25f, 1.0f, 1.0f,   // tr
        -25f, -0f, 25f, 0.0f, 1.0f,   // tl
    )

    val plainIndices = intArrayOf(
        0, 1, 3, 3, 1, 2
    )


    val camera = Camera()
    val inputManager = InputManager(window, camera.iCameraInput)
    val renderer = Renderer(window, WINDOW_WIDTH, WINDOW_HEIGHT)

    val models = arrayListOf<Model>()

    val plain = Model()
    plain.create(plainVertices, plainIndices)
    plain.addTexture("src/main/resources/Textures/grass.png")
    models.add(plain)

    val r = Random(1234)
    for (x in 1..5) {
        val m = Model()
        m.apply {
            create(vertices, indices)
            addTexture("src/main/resources/Textures/cat.png")
            translation = Vec3(r.nextFloat() * 5f - 2f, r.nextFloat() * 5f, r.nextFloat() * 5f - 2f)
        }
        models.add(m)
    }

    val mCenter = Model()
    mCenter.apply {
        create(vertices, indices)
        addTexture("src/main/resources/Textures/cat.png")
        scale = Vec3(0.2f, 20f, 0.2f)
        translation = Vec3(0f, 10f, 0f)
    }
    models.add(mCenter)

    while (!glfwWindowShouldClose(window)) {
        inputManager.update()
        renderer.render(models, camera)
    }

    // Cleanup
    for (m in models) {
        m.cleanup()
    }

    renderer.cleanup()

    // Destroy window
    Debug.logi(TAG, "Destroying window...")
    glfwDestroyWindow(window)
    inputManager.cleanup()
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