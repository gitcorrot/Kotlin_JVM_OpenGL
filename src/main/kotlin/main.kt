import glm_.vec2.Vec2
import glm_.vec3.Vec3
import model.Mesh
import model.Vertex
import org.lwjgl.Version.getVersion
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
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

    glfwSetInputMode(window, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE)
    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
    glfwMakeContextCurrent(window)
    glfwSwapInterval(1) // Enable v-sync
    GL.createCapabilities()

    Debug.logi(TAG, "JLWGL Version: ${getVersion()}")
    Debug.logi(TAG, "OpenGL Version: ${glGetString(GL_VERSION)}")

    // -------------------------------------------------------------------------------------------------------------- //

    val camera = Camera()
    val inputManager = InputManager(window, camera.iCameraInput)
    val renderer = Renderer(window, WINDOW_WIDTH, WINDOW_HEIGHT)

    val colorPaletteTexture = Texture()
    colorPaletteTexture.createTexture("src/main/resources/Textures/color_palette.png")

    val models = arrayListOf<Model>()

    val plain = Model()
    val plainVertices: ArrayList<Vertex> = arrayListOf(
        Vertex(Vec3(-25f, -0f, -25f), Vec3(0f), Vec2(0.0f, 0.0f)),
        Vertex(Vec3(25f, -0f, -25f), Vec3(0f), Vec2(1f, 0.0f)),
        Vertex(Vec3(25f, -0f, 25f), Vec3(0f), Vec2(1f, 1f)),
        Vertex(Vec3(-25f, -0f, 25f), Vec3(0f), Vec2(0.0f, 1f)),
    )

    val plainIndices = intArrayOf(0, 1, 3, 3, 1, 2)
    plain.create(Mesh(plainVertices, plainIndices))
    plain.addTexture(colorPaletteTexture)
    models.add(plain)

    // Model from: http://quaternius.com/
    val testModel = ModelLoader.loadStaticModel("src/main/resources/Models/PIG.obj")
    testModel.addTexture("src/main/resources/Textures/color_palette.png")
    models.add(testModel)

    while (!glfwWindowShouldClose(window)) {
        inputManager.update()
        renderer.render(models, camera)
    }

    readOpenGLError()
        
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