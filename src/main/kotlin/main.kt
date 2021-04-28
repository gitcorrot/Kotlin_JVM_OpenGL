import data.Mesh
import data.Vertex
import glm_.glm
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import light.LightAmbient
import light.LightDirectional
import light.LightPoint
import light.LightSpot
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
import kotlin.random.Random

const val TAG = "Main"

const val WINDOW_WIDTH = 1000
const val WINDOW_HEIGHT = 800

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
    world.addModelDefault(plain)

    // Model from: http://quaternius.com/
    val pigMesh = ModelLoader.loadStaticModel("src/main/resources/Models/PIG.obj")
    val r = Random(1234)
    for (i in 1..15) {
        val pigModel = ModelDefault(pigMesh)
        pigModel.create()
        pigModel.addTexture(colorPaletteTexture)
        world.addModelDefault(pigModel)
        pigModel.rotateBy(r.nextFloat() * 360f, 0f, 0f)
        pigModel.moveTo(r.nextFloat() * 100f - 50f, 0f, r.nextFloat() * 100f - 50f)
    }

    // light.Light source model
    val lampMesh = ModelLoader.loadStaticModel("src/main/resources/Models/sphere.obj")
    val lampModel1 = ModelNoLight(lampMesh)
    lampModel1.create()
    lampModel1.addTexture(colorPaletteTexture)
    lampModel1.moveTo(-15f, 5f, 0f)
    world.addModelNoLight(lampModel1)
    val lampModel2 = ModelNoLight(lampMesh)
    lampModel2.create()
    lampModel2.addTexture(colorPaletteTexture)
    lampModel2.moveTo(15f, 5f, 0f)
    world.addModelNoLight(lampModel2)

    val streetLampMesh = ModelLoader.loadStaticModel("src/main/resources/Models/street_lamp.obj")
    val streetLampModel = ModelDefault(streetLampMesh)
    streetLampModel.create()
    streetLampModel.addTexture(colorPaletteTexture)
    streetLampModel.scaleTo(4f, 4f, 4f)
    streetLampModel.moveTo(-5f, 20f, -20f)
    world.addModelDefault(streetLampModel)

    // Lighting
    val ambientLight = LightAmbient()
    ambientLight.color = Vec3(1f, 1f, 1f)
    ambientLight.intensity = 0.4f
    world.addLightSource(ambientLight)

    val directionalLight = LightDirectional()
    directionalLight.color = Vec3(1f, 1f, 1f)
    directionalLight.intensity = 0.4f
    directionalLight.direction = Vec3(-1.0f, -0.5f, -0.5f)
    world.addLightSource(directionalLight)

    val pointLight1 = LightPoint(0)
    pointLight1.color = Vec3(.2f, .2f, 1f)
    pointLight1.intensity = 1f
    pointLight1.position = lampModel1.transformation.translation
    pointLight1.kc = 1.0f
    pointLight1.kl = 0.05f
    pointLight1.kq = 0.05f
    world.addLightSource(pointLight1)

    val pointLight2 = LightPoint(1)
    pointLight2.color = Vec3(1f, 0.5f, 0f)
    pointLight2.intensity = 1f
    pointLight2.position = lampModel2.transformation.translation
    pointLight2.kc = 1.0f
    pointLight2.kl = 0.05f
    pointLight2.kq = 0.05f
    world.addLightSource(pointLight2)

    val spotLight = LightSpot(0)
    spotLight.color = Vec3(1f, 1f, 1f)
    spotLight.intensity = 0.3f
    spotLight.position  = streetLampModel.transformation.translation
    spotLight.direction = Vec3(0f, -1f, 0f)
    spotLight.outerAngle = glm.cos(glm.radians(45f))
    spotLight.innerAngle = glm.cos(glm.radians(30f))
    world.addLightSource(spotLight)


    // Skybox
    val skybox = Skybox()
    world.skybox = skybox

    while (!glfwWindowShouldClose(window)) {
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
