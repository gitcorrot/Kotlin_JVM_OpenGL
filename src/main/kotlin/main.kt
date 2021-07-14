import glm_.glm
import glm_.vec3.Vec3
import light.LightAmbient
import light.LightDirectional
import light.LightPoint
import light.LightSpot
import models.ModelDefault
import models.ModelNoLight
import models.Quad
import org.lwjgl.Version.getVersion
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL33.*
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.MemoryUtil.NULL
import utils.Debug
import utils.ModelLoader
import utils.OpenGLUtils
import utils.OpenGLUtils.readOpenGLError
import utils.ResourcesUtils
import java.nio.FloatBuffer
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

    val inputManager = InputManager(window)
    val camera = Camera()
    inputManager.addCamera(camera)
    val renderer = Renderer(window, WINDOW_WIDTH, WINDOW_HEIGHT)

    val colorPaletteTexture = Texture()
    colorPaletteTexture.createTexture("src/main/resources/Textures/color_palette.png")

    val world = World()

//    Debug.logd(TAG, "TERRAIN")
    val terrain = Terrain(0.05f, 2.0f)
    terrain.generateMesh(10) // 3x3=9 squares -> 3x3x2=18 triangles -> 4x4=16 vertices -> 3x3x3x2=54 indices
    terrain.create()
//    world.addTerrain(terrain)

    // Coordinate system origin helpful model
    Debug.logd(TAG, "COORDINATE SYSTEM")
    val cs = ModelLoader.loadStaticModel("src/main/resources/Models/cs.obj")
    val csModel = ModelDefault(cs)
    csModel.create()
    csModel.addTexture(colorPaletteTexture)
//    world.addModelNoLight(csModel)

    // Tree
    Debug.logd(TAG, "TREE")
    val treeMesh = ModelLoader.loadStaticModel("src/main/resources/Models/tree1.obj")
    val treeModel = ModelDefault(treeMesh)
    treeModel.create()
    treeModel.addTexture(colorPaletteTexture)
    treeModel.moveTo(10f, terrain.getHeightAt(10, -6), -6f)
//    world.addModelDefault(treeModel)

//    // Model from: http://quaternius.com/
//    val pigMesh = ModelLoader.loadStaticModel("src/main/resources/Models/pig.obj")
//    val r = Random(12345)
//    for (i in 1..5) {
//        val pigModel = ModelDefault(pigMesh)
//        pigModel.create()
//        pigModel.addTexture(colorPaletteTexture)
//        world.addModelDefault(pigModel)
//        pigModel.rotateTo(r.nextFloat() * 360f, 0f, 0f)
//        val scale = r.nextInt(5, 10) / 10f
//        pigModel.scaleTo(scale)
//        val randX = r.nextFloat() * terrain.size * terrain.tileSize
//        val randZ = -r.nextFloat() * terrain.size * terrain.tileSize
//        val height = terrain.getHeightAt(randX.toInt(), randZ.toInt())
//        pigModel.moveTo(randX, height, randZ)
//    }

    // Light source model
    val lampMesh = ModelLoader.loadStaticModel("src/main/resources/Models/sphere.obj")
    // Yellow
    val lampModel1 = ModelNoLight(lampMesh)
    lampModel1.create()
    lampModel1.addTexture(colorPaletteTexture)
    lampModel1.scaleTo(0.3f, 0.3f, 0.3f)
    lampModel1.moveTo(2f, terrain.getHeightAt(2, -2)+ 3f, -2f)
    world.addModelNoLight(lampModel1)
    // Red
    val lampModel2 = ModelNoLight(lampMesh)
    lampModel2.create()
    lampModel2.addTexture(colorPaletteTexture)
    lampModel2.scaleTo(0.3f, 0.3f, 0.3f)
    lampModel2.moveTo(19f, terrain.getHeightAt(19, -19) + 3f, -19f)
//    world.addModelNoLight(lampModel2)

    val streetLampMesh = ModelLoader.loadStaticModel("src/main/resources/Models/street_lamp.obj")
    val streetLampModel = ModelDefault(streetLampMesh)
    streetLampModel.create()
    streetLampModel.addTexture(colorPaletteTexture)
    streetLampModel.moveTo(10f, terrain.getHeightAt(10, -10), -10f)
//    world.addModelDefault(streetLampModel)

    // Lighting
    val ambientLight = LightAmbient()
    ambientLight.color = Vec3(1f, 1f, 1f)
    ambientLight.intensity = 0.5f
//    world.addLightSource(ambientLight)

    val directionalLight = LightDirectional()
    directionalLight.color = Vec3(1f, 1f, 1f)
    directionalLight.intensity = 0.2f
    directionalLight.direction = Vec3(-1.0f, -0.5f, -0.5f)
//    world.addLightSource(directionalLight)

    // Blue
    val pointLight1 = LightPoint(0, lampModel1.position)
    pointLight1.color = Vec3(0f, 0f, 1f)
    pointLight1.intensity = 1f
    pointLight1.kc = 1.0f
    pointLight1.kl = 0.14f
    pointLight1.kq = 0.07f
//    world.addLightSource(pointLight1)

    // Red
    val pointLight2 = LightPoint(1, lampModel2.position)
    pointLight2.color = Vec3(1f, 0.0f, 0.0f)
    pointLight2.intensity = 1f
    pointLight2.kc = 1.0f
    pointLight2.kl = 0.14f
    pointLight2.kq = 0.07f
//    world.addLightSource(pointLight2)
//
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
//    world.addLightSource(spotLight)
//
//
//    // Skybox
//    val skybox = Skybox()
//    world.skybox = skybox


    val gShader = ShaderProgram()
    val vertexShaderPath = "g_buffer_vertex_shader.glsl"
    val fragmentShaderPath = "g_buffer_fragment_shader.glsl"
    val vertexShaderString = ResourcesUtils.readShader(vertexShaderPath)
    val fragmentShaderString = ResourcesUtils.readShader(fragmentShaderPath)
    gShader.createShader(vertexShaderString, GL_VERTEX_SHADER)
    gShader.createShader(fragmentShaderString, GL_FRAGMENT_SHADER)
    gShader.link()

//    val terrainGShader = ShaderProgram()
//    val terrainGShaderVertexShaderPath = "terrain_vertex_shader.glsl"
//    val terrainGShaderGeometryShaderPath = "terrain_geometry_shader.glsl"
//    val terrainGShaderFragmentShaderPath = "terrain_fragment_shader.glsl"
//    val terrainGShaderVertexShaderString = ResourcesUtils.readShader(terrainGShaderVertexShaderPath)
//    val terrainGShaderGeometryShaderString = ResourcesUtils.readShader(terrainGShaderGeometryShaderPath)
//    val terrainGShaderFragmentShaderString = ResourcesUtils.readShader(terrainGShaderFragmentShaderPath)
//    terrainGShader.createShader(terrainGShaderVertexShaderString, GL_VERTEX_SHADER)
//    terrainGShader.createShader(terrainGShaderGeometryShaderString, GL_GEOMETRY_SHADER)
//    terrainGShader.createShader(terrainGShaderFragmentShaderString, GL_FRAGMENT_SHADER)
//    terrainGShader.link()

    val lightingShader = ShaderProgram()
    val lightingVertexShaderPath = "deferred_shading_vertex_shader.glsl"
    val lightingFragmentShaderPath = "deferred_shading_fragment_shader.glsl"
    val lightingVertexShaderString = ResourcesUtils.readShader(lightingVertexShaderPath)
    val lightingFragmentShaderString = ResourcesUtils.readShader(lightingFragmentShaderPath)
    lightingShader.createShader(lightingVertexShaderString, GL_VERTEX_SHADER)
    lightingShader.createShader(lightingFragmentShaderString, GL_FRAGMENT_SHADER)
    lightingShader.link()

    val gBuffer = glGenFramebuffers()
    glBindFramebuffer(GL_FRAMEBUFFER, gBuffer)

    // Position
    val gPosition = glGenTextures()
    glBindTexture(GL_TEXTURE_2D, gPosition)
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, WINDOW_WIDTH, WINDOW_HEIGHT, 0, GL_RGB, GL_FLOAT, NULL)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, gPosition, 0)

    // Normal
    val gNormal = glGenTextures()
    glBindTexture(GL_TEXTURE_2D, gNormal)
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, WINDOW_WIDTH, WINDOW_HEIGHT, 0, GL_RGB, GL_FLOAT, NULL)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, gNormal, 0)

    // Color
    val gColor = glGenTextures()
    glBindTexture(GL_TEXTURE_2D, gColor)
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, WINDOW_WIDTH, WINDOW_HEIGHT, 0, GL_RGB, GL_FLOAT, NULL)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT2, GL_TEXTURE_2D, gColor, 0)

    val buffers = intArrayOf(GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2)
    glDrawBuffers(buffers)

    // Depth buffer
    val rboDepth = glGenRenderbuffers()
    glBindRenderbuffer(GL_RENDERBUFFER, rboDepth)
    glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, WINDOW_WIDTH, WINDOW_HEIGHT)
    glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rboDepth)
    if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
        Debug.loge(TAG, "Framebuffer not complete!")
    } else {
        Debug.logi(TAG, "Framebuffer complete")
    }
    glBindFramebuffer(GL_FRAMEBUFFER, 0)


    // ------------------- Configure shaders- ----------------------------
    gShader.use()
    // Shader's sampler2d belong to texture unit 2
    gShader.setUniformInt("colorPaletteTexture", 2)

    // Set texture units IDs to Samplers 2D (for MRT)
    lightingShader.use()
    lightingShader.setUniformInt("gPos", 0)
    lightingShader.setUniformInt("gNor", 1)
    lightingShader.setUniformInt("gCol", 2)
    // --------------------------------------------------------------------

    glDepthFunc(GL_LESS)

    val fov: Float = glm.radians(60f)
    val aspectRatio: Float = WINDOW_WIDTH / WINDOW_HEIGHT.toFloat()
    val zNear = 0.1f
    val zFar = 1000.0f
    val projectionMat = glm.perspective(fov, aspectRatio, zNear, zFar)

    // Quad for drawing FrameBuffer on screen
    val quad = Quad()

    while (!glfwWindowShouldClose(window)) {
        inputManager.update()

        glEnable(GL_DEPTH_TEST)

        // 1. Geometry pass
        glBindFramebuffer(GL_FRAMEBUFFER, gBuffer)
        glClearColor(0f, 0f, 0f, 1f)
//        glClearColor(0.6f, 0.6f, 1f, 1f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        var currentShader = gShader

        for (model in listOf(
            terrain,
            treeModel,
            csModel,
            streetLampModel,
            lampModel1,
            lampModel2)
        ) {
            currentShader = if (model == terrain) {
                Terrain.shaderProgram
            } else {
                gShader
            }

            currentShader.use()
            val modelTransMat = model.transformationMat
            currentShader.setUniformMat4f("m", modelTransMat)
            currentShader.setUniformMat4f("v", camera.viewMat)
            currentShader.setUniformMat4f("p", projectionMat)

            if (model != terrain) {
                val modelNormalMat = glm.transpose(glm.inverse(modelTransMat.toMat3()))
                currentShader.setUniformMat3f("normalMatrix", modelNormalMat)

                // tutaj aktywuję texture unit 2
                glActiveTexture(GL_TEXTURE2)
                // a tutaj do tego texture unitu przypisujemy texture ('colorPaletteTexture')
                model.texture.bind()
            }

            model.bind()
            // a tutaj wypełniam ją kolorem
            glDrawElements(GL_TRIANGLES, model.getIndicesCount(), GL_UNSIGNED_INT, 0)
        }
        glBindVertexArray(0)

        // 2. Lighting pass
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        glClearColor(0f, 0f, 0f, 1f)
        glClear(GL_COLOR_BUFFER_BIT)
        lightingShader.use()
        glDisable(GL_DEPTH_TEST)
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, gPosition)
        glActiveTexture(GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, gNormal)
        glActiveTexture(GL_TEXTURE2)
        glBindTexture(GL_TEXTURE_2D, gColor)

        lightingShader.setUniformInt("noPointLights", 2)
        lightingShader.setUniformInt("noSpotLights", 1)

        ambientLight.apply(lightingShader)
        directionalLight.apply(lightingShader)
        pointLight1.apply(lightingShader)
        pointLight2.apply(lightingShader)
        spotLight.apply(lightingShader)

        quad.draw()

        glfwSwapBuffers(window)
    }

    // Cleanup
    world.cleanup()

    // Read OpenGL error
    readOpenGLError()?.let { openGlError ->
        Debug.loge(TAG, openGlError)
    }

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


