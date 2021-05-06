import glm_.glm
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
    Debug.DEBUG_LEVEL = Debug.DebugLevel.DEBUG

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

    Debug.logd(TAG, "TERRAIN")
    val terrain = Terrain(0.15f, 10f)
    terrain.generateMesh(10) // 3x3=9 squares -> 3x3x2=18 triangles -> 4x4=16 vertices -> 3x3x3x2=54 indices
    terrain.create()
    Debug.logd(TAG, terrain.mesh.toString())
    world.addTerrain(terrain)

    // Model from: http://quaternius.com/
    val pigMesh = ModelLoader.loadStaticModel("src/main/resources/Models/PIG.obj")
    val r = Random(1234)
    for (i in 1..10) {
        val pigModel = ModelDefault(pigMesh)
        pigModel.create()
        pigModel.addTexture(colorPaletteTexture)
        world.addModelDefault(pigModel)
        pigModel.rotateBy(r.nextFloat() * 360f, 0f, 0f)
        val randX = r.nextFloat() * 10f * 10f
        val randZ = r.nextFloat() * 10f * 10f
        val height = terrain.getHeightAt((randX / 10f).toInt(), (randZ / 10f).toInt())
        pigModel.moveTo(randX, height, randZ)
    }

    // Light source model
    val lampMesh = ModelLoader.loadStaticModel("src/main/resources/Models/sphere.obj")
    val lampModel1 = ModelNoLight(lampMesh)
    lampModel1.create()
    lampModel1.addTexture(colorPaletteTexture)
    lampModel1.moveTo(90f, 3f, 90f)
    world.addModelNoLight(lampModel1)
    val lampModel2 = ModelNoLight(lampMesh)
    lampModel2.create()
    lampModel2.addTexture(colorPaletteTexture)
    lampModel2.moveTo(10f, 10f, 10f)
    world.addModelNoLight(lampModel2)

    val streetLampMesh = ModelLoader.loadStaticModel("src/main/resources/Models/street_lamp.obj")
    val streetLampModel = ModelDefault(streetLampMesh)
    streetLampModel.create()
    streetLampModel.addTexture(colorPaletteTexture)
    streetLampModel.scaleTo(4f, 4f, 4f)
    streetLampModel.moveTo(50f, terrain.getHeightAt(4, 4) + 20f, 50f)
    world.addModelDefault(streetLampModel)

    // Lighting
    val ambientLight = LightAmbient()
    ambientLight.color = Vec3(1f, 1f, 1f)
    ambientLight.intensity = 0.5f
    world.addLightSource(ambientLight)

    val directionalLight = LightDirectional()
    directionalLight.color = Vec3(1f, 1f, 1f)
    directionalLight.intensity = 0.5f
    directionalLight.direction = Vec3(-1.0f, -0.5f, -0.5f)
    world.addLightSource(directionalLight)

    val pointLight1 = LightPoint(0)
    pointLight1.color = Vec3(0f, 0f, 1f)
    pointLight1.intensity = 1.0f
    pointLight1.position = lampModel1.transformation.translation
    pointLight1.kc = 1.0f
    pointLight1.kl = 0.015f
    pointLight1.kq = 0.0075f
    world.addLightSource(pointLight1)

    val pointLight2 = LightPoint(1)
    pointLight2.color = Vec3(1f, 0.0f, 0.0f)
    pointLight2.intensity = 1f
    pointLight2.position = lampModel2.transformation.translation
    pointLight2.kc = 1.0f
    pointLight2.kl = 0.015f
    pointLight2.kq = 0.0075f
    world.addLightSource(pointLight2)

    val spotLight = LightSpot(0)
    spotLight.color = Vec3(1f, 1f, 1f)
    spotLight.intensity = 0.3f
    spotLight.position = streetLampModel.transformation.translation
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
