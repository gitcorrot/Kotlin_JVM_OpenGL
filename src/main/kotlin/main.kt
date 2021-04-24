import data.Mesh
import data.Vertex
import glm_.Java.Companion.glm
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import models.ModelDefault
import models.ModelNoLight
import org.lwjgl.Version.getVersion
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL33.*
import org.lwjgl.system.MemoryUtil.NULL
import utils.Debug
import utils.ModelLoader

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
    Debug.DEBUG_LEVEL = Debug.DebugLevel.INFO

    val errCallback = glfwSetErrorCallback(GLFWErrorCallback.create { error, description ->
        Debug.loge(TAG, "Error $error: $description")
    })

    if (!glfwInit()) {
        throw IllegalStateException("Unable to initialize GLFW")
    }

    val window = createWindow()

    // Get the resolution of the primary monitor
    val vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())

    vidMode?.let {
        // Center our window
        glfwSetWindowPos(
            window,
            (it.width() - WINDOW_WIDTH) / 2,
            (it.height() - WINDOW_HEIGHT) / 2
        )
        Debug.logd(TAG, "${it.width()}X${it.height()}")
    }

    glfwSetInputMode(window, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE)
    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
    glfwMakeContextCurrent(window)
    glfwSwapInterval(1) // Enable v-sync
    GL.createCapabilities()

    Debug.logi(TAG, "JLWGL Version: ${getVersion()}")
    Debug.logi(TAG, "OpenGL Version: ${glGetString(GL_VERSION)}")

    // -------------------------------------------------------------------------------------------------------------- //

    val inputManager = InputManager(window)
    val camera = Camera()
    inputManager.addCamera(camera)
    val renderer = Renderer(window, WINDOW_WIDTH, WINDOW_HEIGHT)

    val colorPaletteTexture = Texture()
    colorPaletteTexture.createTexture("src/main/resources/Textures/color_palette.png")

    val world = World()

    // TODO: replace with terrain
    val plainVertices: ArrayList<Vertex> = arrayListOf(
        Vertex(Vec3(-1000f, -0f, -1000), Vec3(0f, 1f, 0.0f), Vec2(0.5f, 0.5f)),
        Vertex(Vec3(1000, -0f, -1000), Vec3(0f, 1f, 0.0f), Vec2(0.51f, 0.5f)),
        Vertex(Vec3(1000, -0f, 1000), Vec3(0f, 1f, 0.0f), Vec2(.51f, .51f)),
        Vertex(Vec3(-1000, -0f, 1000), Vec3(0f, 1f, 0.0f), Vec2(0.5f, .51f)),
    )
    val plainIndices = intArrayOf(0, 1, 3, 3, 1, 2)
    val plain = ModelDefault(Mesh(plainVertices, plainIndices))
    plain.create()
    plain.addTexture(colorPaletteTexture)
    world.addDefaultModel(plain)

    // Model from: http://quaternius.com/
    val pigMesh = ModelLoader.loadStaticModel("src/main/resources/Models/PIG.obj")
    val pigModel = ModelDefault(pigMesh)
    pigModel.create()
    pigModel.addTexture(colorPaletteTexture)
    world.addDefaultModel(pigModel)

    // Light source model
    val pointLightMesh = ModelLoader.loadStaticModel("src/main/resources/Models/sphere.obj")
    val pointLight = ModelNoLight(pointLightMesh)
    pointLight.create()
    pointLight.addTexture(colorPaletteTexture)
    world.addLightSource(pointLight)

    // Skybox
    val skybox = Skybox()
    world.skybox = skybox

    while (!glfwWindowShouldClose(window)) {
        val x = glm.sin(glfwGetTime() * 2.0f).toFloat()
        pigModel.rotateBy(0f, 2f, 0f)
        pointLight.moveTo(20.0f, 10.0f, 0f)
        inputManager.update()
        renderer.render(world, camera)
    }

    readOpenGLError()

    // Cleanup
    world.cleanup()

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
