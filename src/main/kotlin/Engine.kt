import components.*
import glm_.glm
import glm_.quat.Quat
import glm_.toFloat
import glm_.vec3.Vec3
import light.LightAmbient
import light.LightDirectional
import light.LightPoint
import light.LightSpot
import models.base.ModelDefault
import models.base.ModelNoLight
import models.base.Terrain
import org.lwjgl.Version.getVersion
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL33.GL_VERSION
import org.lwjgl.opengl.GL33.glGetString
import org.lwjgl.system.MemoryUtil
import systems.InputSystem
import systems.MoveSystem
import systems.RenderSystem
import utils.Debug
import utils.ModelLoader
import utils.OpenGLUtils.readOpenGLError


class Engine {
    companion object {
        val TAG: String = this::class.java.name
        private const val defaultWindowWidth = 1000
        private const val defaultWindowHeight = 800
    }

    private val window: Long
    private val ecs = ECS()
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
        Debug.logi(TAG, "------------------------------------------------")

        InputSystem.attachToWindow(window)
        RenderSystem.attachToWindow(window)

        // order is important!
        ecs.addSystem(InputSystem)
        ecs.addSystem(MoveSystem)
        ecs.addSystem(RenderSystem)

        initWorld()
    }

    fun run() {
        var currentTime: Double
        var lastFrameTime = glfwGetTime() - 16.6f

        while (!glfwWindowShouldClose(window)) {
            currentTime = glfwGetTime()

            ecs.update((currentTime - lastFrameTime).toFloat * 1000f)

            // Read OpenGL error
            readOpenGLError()?.let { openGlError ->
                Debug.loge(TAG, openGlError)
            }

            lastFrameTime = currentTime
        }

        cleanup()
    }

    private fun cleanup() {
        ecs.cleanup()
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

    private fun initWorld() {
        val terrain = Terrain(10, 0.1f, 1f)

        ecs.addEntity(
            Entity()
                .addComponent(
                    PositionComponent().apply {
                        position = Vec3(5f, terrain.getHeightAt(5f, -5f), -5f)
                        rotation = Quat(glm.radians(Vec3(0f)))
                    })
                .addComponent(
                    ModelComponent(
                        ModelDefault(
                            mesh = ModelLoader.loadStaticModel("src/main/resources/Models/tree1.obj"),
                            texture = Texture.getDefaultColorPalette()
                        )
                    )
                )
                .addComponent(
                    VelocityComponent(Vec3(-0.001f, 0f, -0.001f))
                )
        )

        ecs.addEntity(
            Entity()
                .addComponent(
                    PositionComponent().apply {
                        position = Vec3(0f, 0f, 0f)
                        rotation = Quat(glm.radians(Vec3(0f)))
                    })
                .addComponent(
                    ModelComponent(
                        ModelNoLight(
                            mesh = ModelLoader.loadStaticModel("src/main/resources/Models/cs2.obj"),
                            texture = Texture.getDefaultColorPalette()
                        )
                    )
                )
        )
        ecs.addEntity(
            Entity()
                .addComponent(
                    PositionComponent().apply {
                        position = Vec3(0f)
                    }
                )
                .addComponent(
                    ModelComponent(terrain)
                )
        )
        ecs.addEntity(
            Entity()
                .addComponent(
                    CameraComponent()
                )
        )
        // Lights
        ecs.addEntity(
            Entity()
                .addComponent(
                    LightComponent(
                        LightAmbient().apply {
                            color = Vec3(1f, 1f, 1f)
                            intensity = 0.7f
                        })
                )
        )
        ecs.addEntity(
            Entity()
                .addComponent(
                    LightComponent(
                        LightDirectional().apply {
                            color = Vec3(1f, 1f, 1f)
                            intensity = 0.2f
                            direction = Vec3(-1.0f, -0.5f, -0.5f)
                        }
                    )
                )
        )
        ecs.addEntity(
            Entity()
                .addComponent(
                    LightComponent(
                        LightPoint(0, Vec3(0f, 2f, 0f)).apply {
                            color = Vec3(0f, 0f, 1f)
                            intensity = 1f
                            kc = 1.0f
                            kl = 0.14f
                            kq = 0.07f
                        }
                    )
                )
        )
        ecs.addEntity(
            Entity()
                .addComponent(
                    LightComponent(
                        LightSpot(0, Vec3(5f, 2f, 0f)).apply {
                            color = Vec3(0.8f, 0.65f, 0.4f)
                            intensity = 0.3f
                            direction = Vec3(0f, -1f, 0f)
                            outerAngle = glm.cos(glm.radians(50f))
                            innerAngle = glm.cos(glm.radians(20f))
                        }
                    )
                ))

    }
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