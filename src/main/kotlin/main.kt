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
import org.lwjgl.opengl.GL33.GL_VERSION
import org.lwjgl.opengl.GL33.glGetString
import org.lwjgl.system.MemoryUtil.NULL
import utils.Debug
import utils.ModelLoader
import utils.OpenGLUtils.readOpenGLError
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
        Debug.logd(TAG, "Detected display resolution ${it.width()}x${it.height()}")
    }

    glfwSetInputMode(window, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE)
    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
    glfwMakeContextCurrent(window)
    glfwSwapInterval(1) // Enable v-sync
    GL.createCapabilities()

    Debug.logi(TAG, "JLWGL Version: ${getVersion()}")
    Debug.logi(TAG, "OpenGL Version: ${glGetString(GL_VERSION)}")

    // -------------------------------------------------------------------------------------------------------------- //

    val camera = Camera()
    val inputManager = InputManager(window)
    inputManager.addCamera(camera)
    val renderer = Renderer(window, WINDOW_WIDTH, WINDOW_HEIGHT)
    val world = World()
    initWorld(world)

    while (!glfwWindowShouldClose(window)) {
        inputManager.update()
        renderer.render(world, camera)
    }

    // Read OpenGL error
    readOpenGLError()?.let { openGlError ->
        Debug.loge(TAG, openGlError)
    }

    // Cleanup
    world.cleanup()
    Debug.logi(TAG, "Destroying window...")
    glfwDestroyWindow(window)
    inputManager.cleanup()
    errCallback?.free()
    glfwTerminate()
    glfwSetErrorCallback(null)?.free()
    Debug.logi(TAG, "Window destroyed!")
}

private fun initWorld(world: World) {
    val colorPaletteTexture = Texture()
    colorPaletteTexture.createTexture("src/main/resources/Textures/color_palette.png")

    // Terrain
    val terrain = Terrain(0.05f, 2.0f)
    terrain.generateMesh(10) // 3x3=9 squares -> 3x3x2=18 triangles -> 4x4=16 vertices -> 3x3x3x2=54 indices
    terrain.create()
    world.addTerrain(terrain)

    // Coordinate system origin helpful model
    Debug.logd(TAG, "COORDINATE SYSTEM")
    val cs = ModelLoader.loadStaticModel("src/main/resources/Models/cs.obj")
    val csModel = ModelNoLight(cs)
    csModel.create()
    csModel.addTexture(colorPaletteTexture)
    world.addModelNoLight(csModel)

    // Tree
    Debug.logd(TAG, "TREE")
    val treeMesh = ModelLoader.loadStaticModel("src/main/resources/Models/tree1.obj")
    val treeModel = ModelDefault(treeMesh)
    treeModel.create()
    treeModel.addTexture(colorPaletteTexture)
    treeModel.moveTo(10f, terrain.getHeightAt(10, -6), -6f)
    world.addModelDefault(treeModel)

    // Rock
    Debug.logd(TAG, "ROCK")
    val rockMesh = ModelLoader.loadStaticModel("src/main/resources/Models/rock1.obj")
    val rockModel = ModelDefault(rockMesh)
    rockModel.create()
    rockModel.addTexture(colorPaletteTexture)
    rockModel.moveTo(5f, terrain.getHeightAt(5, -12), -12f)
    world.addModelDefault(rockModel)

    // Pig model from: http://quaternius.com/
    val pigMesh = ModelLoader.loadStaticModel("src/main/resources/Models/pig.obj")
    val r = Random(1234567)
    for (i in 1..5) {
        val pigModel = ModelDefault(pigMesh)
        pigModel.create()
        pigModel.addTexture(colorPaletteTexture)
        world.addModelDefault(pigModel)
        pigModel.rotateTo(r.nextFloat() * 360f, 0f, 0f)
//        val scale = r.nextInt(5, 10) / 10f
//        pigModel.scaleTo(scale)
        val randX = r.nextFloat() * terrain.size * terrain.tileSize
        val randZ = -r.nextFloat() * terrain.size * terrain.tileSize
        val height = terrain.getHeightAt(randX.toInt(), randZ.toInt())
        pigModel.moveTo(randX, height, randZ)
    }

    // Light source model
    val lampMesh = ModelLoader.loadStaticModel("src/main/resources/Models/sphere.obj")
    val lampModel1 = ModelNoLight(lampMesh)
    lampModel1.create()
    lampModel1.addTexture(colorPaletteTexture)
    lampModel1.scaleTo(0.3f, 0.3f, 0.3f)
    lampModel1.moveTo(2f, terrain.getHeightAt(2, -2) + 3f, -2f)
    world.addModelNoLight(lampModel1)

    val lampModel2 = ModelNoLight(lampMesh)
    lampModel2.create()
    lampModel2.addTexture(colorPaletteTexture)
    lampModel2.scaleTo(0.3f, 0.3f, 0.3f)
    lampModel2.moveTo(19f, terrain.getHeightAt(19, -19) + 3f, -19f)
    world.addModelNoLight(lampModel2)

    val streetLampMesh = ModelLoader.loadStaticModel("src/main/resources/Models/street_lamp.obj")
    val streetLampModel = ModelDefault(streetLampMesh)
    streetLampModel.create()
    streetLampModel.addTexture(colorPaletteTexture)
    streetLampModel.moveTo(10f, terrain.getHeightAt(10, -10), -10f)
    world.addModelDefault(streetLampModel)

    // Lighting
    val ambientLight = LightAmbient()
    ambientLight.color = Vec3(1f, 1f, 1f)
    ambientLight.intensity = 0.5f
    world.addLightSource(ambientLight)

    val directionalLight = LightDirectional()
    directionalLight.color = Vec3(1f, 1f, 1f)
    directionalLight.intensity = 0.2f
    directionalLight.direction = Vec3(-1.0f, -0.5f, -0.5f)
    world.addLightSource(directionalLight)

    val pointLight1 = LightPoint(0, lampModel1.position)
    pointLight1.color = Vec3(0f, 0f, 1f)
    pointLight1.intensity = 1f
    pointLight1.kc = 1.0f
    pointLight1.kl = 0.14f
    pointLight1.kq = 0.07f
    world.addLightSource(pointLight1)

    val pointLight2 = LightPoint(1, lampModel2.position)
    pointLight2.color = Vec3(1f, 0.0f, 0.0f)
    pointLight2.intensity = 1f
    pointLight2.kc = 1.0f
    pointLight2.kl = 0.14f
    pointLight2.kq = 0.07f
    world.addLightSource(pointLight2)

    val spotLight = LightSpot(
        index = 0,
        position = Vec3(
            streetLampModel.position.x,
            streetLampModel.position.y + 2.75f,
            streetLampModel.position.z + 1.4f
        )
    )
    spotLight.color = Vec3(0.8f, 0.65f, 0.4f)
    spotLight.intensity = 0.3f
    spotLight.direction = Vec3(0f, -1f, 0f)
    spotLight.outerAngle = glm.cos(glm.radians(50f))
    spotLight.innerAngle = glm.cos(glm.radians(20f))
    world.addLightSource(spotLight)

    // Skybox
    val skybox = Skybox()
    world.skybox = skybox
}
