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
    val terrain = Terrain(0.05f, 2.0f)
    terrain.generateMesh(10) // 3x3=9 squares -> 3x3x2=18 triangles -> 4x4=16 vertices -> 3x3x3x2=54 indices
    terrain.create()
//    Debug.logd(TAG, terrain.mesh.toString())
    world.addTerrain(terrain)


    // Coordinate system origin helpfull model
    Debug.logd(TAG, "CS")
    val cs = ModelLoader.loadStaticModel("src/main/resources/Models/cs.obj")
    val csModel1 = ModelNoLight(cs)
    csModel1.create()
    csModel1.addTexture(colorPaletteTexture)
    csModel1.moveTo(0f, 0f, 0f)
    world.addModelNoLight(csModel1)

    // Tree
    Debug.logd(TAG, "TREE")
    val treeMesh = ModelLoader.loadStaticModel("src/main/resources/Models/tree1.obj")
    val treeModel = ModelDefault(treeMesh)
    treeModel.create()
    treeModel.addTexture(colorPaletteTexture)
    treeModel.moveTo(4f, terrain.getHeightAt(2, 2), -4f)
    world.addModelDefault(treeModel)

    // Model from: http://quaternius.com/
    val pigMesh = ModelLoader.loadStaticModel("src/main/resources/Models/PIG.obj")
    val r = Random(12345)
    for (i in 1..5) {
        val pigModel = ModelDefault(pigMesh)
        pigModel.create()
        pigModel.addTexture(colorPaletteTexture)
        world.addModelDefault(pigModel)
        pigModel.rotateBy(r.nextFloat() * 360f, 0f, 0f)
        val scale = r.nextInt(5, 10) / 10f
        pigModel.scaleTo(scale)
        val randX = r.nextFloat() * terrain.size * terrain.tileSize
        val randZ = -r.nextFloat() * terrain.size * terrain.tileSize
        val height = terrain.getHeightAt((randX / terrain.tileSize).toInt(), -(randZ / terrain.tileSize).toInt())
        pigModel.moveTo(randX, height, randZ)
    }

    // Light source model
    val lampMesh = ModelLoader.loadStaticModel("src/main/resources/Models/sphere.obj")

    // Yellow
    val lampModel1 = ModelNoLight(lampMesh)
    lampModel1.create()
    lampModel1.addTexture(colorPaletteTexture)
    lampModel1.moveTo(1f, 3f, -1f)
    world.addModelNoLight(lampModel1)
    // Red
    val lampModel2 = ModelNoLight(lampMesh)
    lampModel2.create()
    lampModel2.addTexture(colorPaletteTexture)
    lampModel2.moveTo(19f, 3f, -19f)
    world.addModelNoLight(lampModel2)

    val streetLampMesh = ModelLoader.loadStaticModel("src/main/resources/Models/street_lamp.obj")
    val streetLampModel = ModelDefault(streetLampMesh)
    streetLampModel.create()
    streetLampModel.addTexture(colorPaletteTexture)
    streetLampModel.moveTo(10f, terrain.getHeightAt(5, 5), -10f)
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

    // Yellow
    val pointLight1 = LightPoint(0, lampModel1.translation)
    pointLight1.color = Vec3(1f, 1f, 0f)
    pointLight1.intensity = 1.0f
    pointLight1.kc = 1.0f
    pointLight1.kl = 0.22f
    pointLight1.kq = 0.20f
    world.addLightSource(pointLight1)

    // Red
    val pointLight2 = LightPoint(1, lampModel2.translation)
    pointLight2.color = Vec3(1f, 0.0f, 0.0f)
    pointLight2.intensity = 1f
    pointLight2.kc = 1.0f
    pointLight2.kl = 0.22f
    pointLight2.kq = 0.20f
    world.addLightSource(pointLight2)

    val spotLight = LightSpot(
        index = 0,
        translation = Vec3(
            streetLampModel.translation.x,
            streetLampModel.translation.y + 2.75f,
            streetLampModel.translation.z + 1.4f
        )
    )
    spotLight.color = Vec3(0.8f, 0.65f, 1f)
    spotLight.intensity = 0.5f
    spotLight.direction = Vec3(0f, -1f, 0f)
    spotLight.outerAngle = glm.cos(glm.radians(50f))
    spotLight.innerAngle = glm.cos(glm.radians(20f))
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
