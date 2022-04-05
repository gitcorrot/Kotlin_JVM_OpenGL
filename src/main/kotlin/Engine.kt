import collision.BoundingBoxType
import data.Movable
import data.Rotatable
import ecs.ECS
import ecs.component.*
import ecs.system.*
import glm_.glm
import glm_.quat.Quat
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import light.LightAmbient
import light.LightDirectional
import light.LightPoint
import light.LightSpot
import models.base.ModelDefault
import models.base.ModelNoLight
import models.base.Terrain
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lwjgl.Version.getVersion
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL33.GL_VERSION
import org.lwjgl.opengl.GL33.glGetString
import org.lwjgl.system.MemoryUtil
import utils.Debug
import utils.ModelLoader
import utils.OpenGLUtils.readOpenGLError
import java.lang.Long.max
import java.util.*
import kotlin.concurrent.schedule
import kotlin.system.measureNanoTime


class Engine(
    private val windowSize: Vec2i,
    private val vSyncEnabled: Boolean
) : KoinComponent {
    companion object {
        private val TAG: String = this::class.java.name
    }

    private val inputSystem by inject<InputSystem>()
    private val dynamicFovSystem by inject<DynamicFovSystem>()
    private val moveSystem by inject<MoveSystem>()
    private val collisionSystem by inject<CollisionSystem>()
    private val renderSystem by inject<RenderSystem>()

    private val window: Long
    private val ecs = ECS()
    private val errCallback: GLFWErrorCallback?

    // debugging
    lateinit var rock: Entity

    init {
        if (!glfwInit()) {
            throw IllegalStateException("Unable to initialize GLFW")
        }

        errCallback = glfwSetErrorCallback(GLFWErrorCallback.create { error, description ->
            Debug.loge(TAG, "Error $error: $description")
        })

        window = createWindow(windowSize.x, windowSize.y)

        glfwMakeContextCurrent(window)
        glfwSwapInterval(if (vSyncEnabled) 1 else 0)
        GL.createCapabilities()

        Debug.logi(TAG, "JLWGL Version: ${getVersion()}")
        Debug.logi(TAG, "OpenGL Version: ${glGetString(GL_VERSION)}")
        Debug.logi(TAG, "------------------------------------------------")

        inputSystem.attachToWindow(window)
        dynamicFovSystem.attachToWindow(window)
        renderSystem.attachToWindow(window)

        // order of systems is important!
        ecs.addSystem(inputSystem, true)
        ecs.addSystem(dynamicFovSystem, true)
        ecs.addSystem(moveSystem, false)
        ecs.addSystem(collisionSystem, false)
        ecs.addSystem(renderSystem, false)

        val initWorldTime = measureNanoTime {
            initWorld()
        }
        Debug.logd(TAG, "World init time:\t\t\t%.3f ms".format(initWorldTime / 1000000f))


        Timer("SettingUp", false).schedule(500) {
            moveSystem.start()
            collisionSystem.start()
            renderSystem.start()
        }
    }

    fun run() {
        var startTime: Double
        var lastFrameTime = glfwGetTime() - 0.0166f
        var deltaTime: Float

        while (!glfwWindowShouldClose(window)) {
            startTime = glfwGetTime()
            deltaTime = (startTime - lastFrameTime).toFloat() * 1000f // ms
            lastFrameTime = startTime

            Debug.logd(TAG, "deltaTime: $deltaTime")

            (rock.getComponent(TransformComponent::class.java.name) as TransformComponent).rotatable.rotateBy(
                0.001f * deltaTime,
                Vec3(0f, 1f, 0f)
            )
            ecs.update(deltaTime)

            // Read OpenGL error
            readOpenGLError()?.let { openGlError ->
                Debug.loge(TAG, openGlError)
            }

            if (!vSyncEnabled) {
                val sleepTime = max(0L, (16.6f - deltaTime).toLong())
                Debug.loge(TAG, "Sleeping for $sleepTime ms")
                Thread.sleep(sleepTime)
            }
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
                (it.width() - windowSize.x) / 2,
                (it.height() - windowSize.y) / 2
            )
            Debug.logi(TAG, "Detected display resolution: ${it.width()}x${it.height()}")
        }

        return window
    }

    private fun initWorld() {
        // Camera
        ecs.addEntity(
            Entity()
                .addComponent(
                    CameraComponent(window)
                )
                .addComponent(
                    TransformComponent().apply {
                        movable.moveTo(0f, 5f, 0f)
                    }
                )
        )

        // Terrain
        val terrain = Terrain(50, 0.1f, 1f)
        ecs.addEntity(
            Entity()
                .addComponent(
                    TransformComponent()
                )
                .addComponent(
                    ModelComponent(terrain)
                )
        )

        // Tree
        ecs.addEntity(
            Entity()
                .addComponent(
                    TransformComponent().apply {
                        movable = Movable(Vec3(5f, terrain.getHeightAt(5f, -10f), -10f))
                        rotatable = Rotatable(Quat(glm.radians(Vec3(0f))))
                    })
                .addComponent(
                    ModelComponent(
                        ModelDefault(
                            mesh = ModelLoader.loadStaticModel("src/main/resources/Models/tree1.obj"),
                            texture = Texture.getDefaultColorPalette()
                        )
                    )
                )
//                .addComponent(
//                    VelocityComponent(Vec3(-0.0001f, 0f, -0.0001f)) // TODO: refactor to 1m/s
//                )
                .addComponent(
                    CollisionComponent(
                        boundingBoxType = BoundingBoxType.AXIS_ALIGNED,
                        optimize = false
                    )
                )
        )

        // Rock
        rock = Entity()
            .addComponent(
                TransformComponent().apply {
                    movable = Movable(Vec3(10f, terrain.getHeightAt(10f, -10f), -10f))
                    rotatable = Rotatable(Quat(glm.radians(Vec3(0f))))
                })
            .addComponent(
                ModelComponent(
                    ModelDefault(
                        mesh = ModelLoader.loadStaticModel("src/main/resources/Models/rock1.obj"),
                        texture = Texture.getDefaultColorPalette()
                    )
                )
            )
            .addComponent(
                CollisionComponent(
                    boundingBoxType = BoundingBoxType.AXIS_ALIGNED,
                    optimize = false
                )
            )
        ecs.addEntity(rock)

        // Coordinate system
        ecs.addEntity(
            Entity()
                .addComponent(
                    TransformComponent()
                )
                .addComponent(
                    ModelComponent(
                        ModelNoLight(
                            mesh = ModelLoader.loadStaticModel("src/main/resources/Models/cs2.obj"),
                            texture = Texture.getDefaultColorPalette()
                        )
                    )
                )
                .addComponent(
                    CollisionComponent(
                        boundingBoxType = BoundingBoxType.AXIS_ALIGNED,
                        optimize = false
                    )
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
                        }
                    )
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
                        LightPoint(0, Movable(Vec3(2f, 1f, -2f))).apply {
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
                        LightSpot(0, Movable(Vec3(7f, 2f, -2f))).apply {
                            color = Vec3(0.8f, 0.65f, 0.4f)
                            intensity = 0.3f
                            direction = Vec3(0f, -1f, 0f)
                            outerAngle = glm.cos(glm.radians(50f))
                            innerAngle = glm.cos(glm.radians(20f))
                        }
                    )
                )
        )
        ecs.addEntity(
            Entity()
                .addComponent(
                    LightComponent(
                        LightSpot(1, Movable(Vec3(12f, 2f, -2f))).apply {
                            color = Vec3(1.0f, 0.0f, 0.0f)
                            intensity = 0.3f
                            direction = Vec3(0f, -1f, 0f)
                            outerAngle = glm.cos(glm.radians(50f))
                            innerAngle = glm.cos(glm.radians(20f))
                        }
                    )
                )
        )
    }
}