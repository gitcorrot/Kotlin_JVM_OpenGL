import components.PositionComponent
import components.VelocityComponent
import glm_.glm
import glm_.quat.Quat
import glm_.vec3.Vec3
import light.LightAmbient
import light.LightDirectional
import light.LightPoint
import light.LightSpot
import models.*
import models.base.Terrain
import nodes.MoveNode
import org.lwjgl.Version.getVersion
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL33.GL_VERSION
import org.lwjgl.opengl.GL33.glGetString
import org.lwjgl.system.MemoryUtil
import systems.PhysicsSystem
import systems.RenderSystem
import utils.Debug
import utils.OpenGLUtils.readOpenGLError
import kotlin.math.roundToInt
import kotlin.random.Random


class Engine {
    companion object {
        val TAG: String = this::class.java.name

        var gravity = false
    }

    private val window: Long
    private val camera: Camera
    private val inputManager: InputManager
    private val renderer: Renderer
//    private val world: World

    private val defaultWindowWidth = 1000
    private val defaultWindowHeight = 800

    private val errCallback: GLFWErrorCallback?

    init {
        if (!glfwInit()) {
            throw IllegalStateException("Unable to initialize GLFW")
        }

        errCallback = glfwSetErrorCallback(GLFWErrorCallback.create { error, description ->
            Debug.loge(TAG, "Error $error: $description")
        })

        window = createWindow(defaultWindowWidth, defaultWindowHeight)

        glfwSetInputMode(window, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE)
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
        glfwMakeContextCurrent(window)
        glfwSwapInterval(1) // Enable v-sync
        GL.createCapabilities()

        Debug.logi(TAG, "JLWGL Version: ${getVersion()}")
        Debug.logi(TAG, "OpenGL Version: ${glGetString(GL_VERSION)}")

        camera = Camera()
        inputManager = InputManager(window)
        inputManager.setCamera(camera)
        renderer = Renderer(window)

        val posComponent = PositionComponent()
        posComponent.position = Vec3(5f, 5f, 5f)
        posComponent.rotation = Quat(glm.radians(Vec3(45f, 0f, 0f)))

        val velComponent = VelocityComponent()
        velComponent.velocity = Vec3(1f, 0f, 0f)

        val testEntity = Entity()
        testEntity.addComponent(posComponent)
        testEntity.addComponent(velComponent)

        val ecs = ECS()
        ecs.addSystem(PhysicsSystem)
        ecs.addSystem(RenderSystem)
        ecs.addEntity(testEntity)
        ecs.update(16.6f)

//        val moveNode = MoveNode(
//            testEntity.id,
//            testEntity.getComponent(PositionComponent::class.java.name) as PositionComponent,
//            testEntity.getComponent(VelocityComponent::class.java.name) as VelocityComponent
//        )
//        moveNode.positionComponent.position.y = 123f
//        moveNode.velocityComponent.velocity.y = 234f
//        println("-----------------------")
//        println(posComponent)
//        println(velComponent)
//        println(moveNode)
//        println("-----------------------")
//        println(ecs.getSystemOfClass(PhysicsSystem::class.java.name) as PhysicsSystem)
//        println(ecs.getNodesOfClass(MoveNode::class.java.name))
//        ecs.removeEntity(testEntity)
//        println(ecs.getNodesOfClass(MoveNode::class.java.name))

//        world = World()
//        initWorld()
    }

    fun run() {

        // Main engine loop
        while (!glfwWindowShouldClose(window)) {

//            world.modelsNoLight.first().rotatePitchBy(0.01f)
//            world.modelsNoLight.first().rotateRollBy(0.01f)
//            world.modelsDefault.first().rotateYawBy(0.01f)

            inputManager.update()

            if (gravity) {
//                val terrain = world.terrains.first()
                val posX = camera.position.x
                val posZ = camera.position.z

//                camera.position.y = terrain.getHeightAt(x = posX, z = posZ) + 2f
            }

//            renderer.render(world, camera)

            // Read OpenGL error
            readOpenGLError()?.let { openGlError ->
                Debug.loge(TAG, openGlError)
            }
        }

        cleanup()
    }

    private fun cleanup() {
//        world.cleanup()
        inputManager.cleanup()
        errCallback?.free()
        glfwDestroyWindow(window)
        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }

    private fun createWindow(width: Int, height: Int): Long {
        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)

        // Create the window
        val window = glfwCreateWindow(
            width, height,
            "Kotlin OpenGL",
            MemoryUtil.NULL,
            MemoryUtil.NULL
        )

        if (window == MemoryUtil.NULL) {
            throw RuntimeException("Failed to create the GLFW window")
        }

        // Get the resolution of the primary monitor
        val vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())

        vidMode?.let {
            // Center our window
            glfwSetWindowPos(
                window,
                (it.width() - defaultWindowWidth) / 2,
                (it.height() - defaultWindowHeight) / 2
            )
            Debug.logd(TAG, "Detected display resolution ${it.width()}x${it.height()}")
        }

        return window
    }

//    private fun initWorld() {
//        val colorPaletteTexture = Texture.getDefaultColorPalette()
//
//        // models.Base.Terrain
//        val terrain = Terrain(10, 0.1f, 5.0f)
//        world.addTerrain(terrain)
//
//        // Coordinate system origin helpful model
//        val coordinateSystem = CoordinateSystem()
//        world.addModelNoLight(coordinateSystem)
//
//        // Tree
//        val tree1 = Tree(TreeType.TYPE_1)
//        tree1.moveTo(10f, terrain.getHeightAt(10f, -10f), -10f)
//        world.addModelDefault(tree1)
//        val tree2 = Tree(TreeType.TYPE_2)
//        tree2.moveTo(20f, terrain.getHeightAt(20f, -10f), -10f)
//        world.addModelDefault(tree2)
//        val tree3 = Tree(TreeType.TYPE_3)
//        tree3.moveTo(30f, terrain.getHeightAt(30f, -10f), -10f)
//        world.addModelDefault(tree3)
//
//        // Rock
//        val rock1 = Rock(RockType.TYPE_1)
//        rock1.moveTo(10f, terrain.getHeightAt(10f, -20f), -20f)
//        world.addModelDefault(rock1)
//        val rock2 = Rock(RockType.TYPE_2)
//        rock2.moveTo(20f, terrain.getHeightAt(20f, -20f), -20f)
//        world.addModelDefault(rock2)
//        val rock3 = Rock(RockType.TYPE_3)
//        rock3.moveTo(30f, terrain.getHeightAt(30f, -20f), -20f)
//        world.addModelDefault(rock3)
//
//        // Pig model from: http://quaternius.com/
//        val r = Random(1234567)
//        for (i in 1..5) {
//            val pig = Pig(PigType.TYPE_1)
//            val scale = r.nextInt(8, 10) / 10f
//            val randX = r.nextFloat() * terrain.size * terrain.tileSize
//            val randZ = -r.nextFloat() * terrain.size * terrain.tileSize
//            val height = terrain.getHeightAt(randX, randZ)
//            pig.rotateTo(r.nextFloat() * 360f, 0f, 0f)
//            pig.scaleTo(scale)
//            pig.moveTo(randX, height, randZ)
//            world.addModelDefault(pig)
//        }
//
//        // Light source model
//        val lampModel1 = SphereLamp()
//        lampModel1.addTexture(colorPaletteTexture)
//        lampModel1.scaleTo(0.3f, 0.3f, 0.3f)
//        lampModel1.moveTo(2f, terrain.getHeightAt(2f, -2f) + 3f, -2f)
//        world.addModelNoLight(lampModel1)
//
//        val lampModel2 = SphereLamp()
//        lampModel2.addTexture(colorPaletteTexture)
//        lampModel2.scaleTo(0.3f, 0.3f, 0.3f)
//        lampModel2.moveTo(19f, terrain.getHeightAt(19f, -19f) + 3f, -19f)
//        world.addModelNoLight(lampModel2)
//
//        val streetLamp = StreetLamp()
//        streetLamp.moveTo(5f, terrain.getHeightAt(5f, -5f), -5f)
//        world.addModelDefault(streetLamp)
//
//        // Lighting
//        val ambientLight = LightAmbient()
//        ambientLight.color = Vec3(1f, 1f, 1f)
//        ambientLight.intensity = 0.7f
//        world.addLightSource(ambientLight)
//
//        val directionalLight = LightDirectional()
//        directionalLight.color = Vec3(1f, 1f, 1f)
//        directionalLight.intensity = 0.2f
//        directionalLight.direction = Vec3(-1.0f, -0.5f, -0.5f)
//        world.addLightSource(directionalLight)
//
//        val pointLight1 = LightPoint(0, lampModel1.position)
//        pointLight1.color = Vec3(0f, 0f, 1f)
//        pointLight1.intensity = 1f
//        pointLight1.kc = 1.0f
//        pointLight1.kl = 0.14f
//        pointLight1.kq = 0.07f
//        world.addLightSource(pointLight1)
//
//        val pointLight2 = LightPoint(1, lampModel2.position)
//        pointLight2.color = Vec3(1f, 0.0f, 0.0f)
//        pointLight2.intensity = 1f
//        pointLight2.kc = 1.0f
//        pointLight2.kl = 0.14f
//        pointLight2.kq = 0.07f
//        world.addLightSource(pointLight2)
//
//        val spotLight = LightSpot(
//            index = 0,
//            position = Vec3(
//                streetLamp.position.x,
//                streetLamp.position.y + 2.75f,
//                streetLamp.position.z + 1.4f
//            )
//        )
//        spotLight.color = Vec3(0.8f, 0.65f, 0.4f)
//        spotLight.intensity = 0.3f
//        spotLight.direction = Vec3(0f, -1f, 0f)
//        spotLight.outerAngle = glm.cos(glm.radians(50f))
//        spotLight.innerAngle = glm.cos(glm.radians(20f))
//        world.addLightSource(spotLight)
//
//        // Skybox
//        val skybox = Skybox()
//        world.skybox = skybox
//    }
}