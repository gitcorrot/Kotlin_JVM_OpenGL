package systems

import DefaultBuffer
import Framebuffer
import GBuffer
import ShaderProgram
import glm_.glm
import glm_.mat4x4.Mat4
import light.LightPoint
import light.LightSpot
import models.base.ModelDefault
import models.base.ModelNoLight
import models.base.Terrain
import nodes.CameraNode
import nodes.LightNode
import nodes.RenderNode
import org.lwjgl.glfw.GLFW.glfwSwapBuffers
import org.lwjgl.opengl.GL33.*
import systems.core.BaseSystem
import ui.view.LoadingView
import utils.Debug
import utils.OpenGLUtils.getWindowSize
import utils.ResourcesUtils

object RenderSystem : BaseSystem() {
    val TAG: String = this::class.java.name

    var cameraNodes = mutableListOf<CameraNode>()
    var renderNodes = mutableListOf<RenderNode>()
    var lightNodes = mutableListOf<LightNode>()

    private var window: Long = -1
    private var isAttachedToWindow = false

    private val lightingPassShader = ShaderProgram()

    private lateinit var gBuffer: GBuffer
    private lateinit var defaultBuffer: Framebuffer

    private var windowWidth: Int = 0
    private var windowHeight: Int = 0
    private var aspectRatio: Float = 0f

    private var loadingView: LoadingView? = null


    init {
        initLightingPassShader()
        glDepthFunc(GL_LESS)
    }

    fun attachToWindow(window: Long) {
        this.window = window
        this.isAttachedToWindow = true

        with(getWindowSize(window)) {
            windowWidth = x.toInt()
            windowHeight = y.toInt()
            aspectRatio = x / y
        }
        loadingView = LoadingView(aspectRatio)
        gBuffer = GBuffer(windowWidth, windowHeight)
        defaultBuffer = DefaultBuffer(windowWidth, windowHeight)
    }

    private fun initLightingPassShader() {
        val lightingVertexShaderPath = "lighting_pass_vertex_shader.glsl"
        val lightingFragmentShaderPath = "lighting_pass_fragment_shader.glsl"

        val lightingVertexShaderString = ResourcesUtils.readShader(lightingVertexShaderPath)
        val lightingFragmentShaderString = ResourcesUtils.readShader(lightingFragmentShaderPath)

        lightingPassShader.createShader(lightingVertexShaderString, GL_VERTEX_SHADER)
        lightingPassShader.createShader(lightingFragmentShaderString, GL_FRAGMENT_SHADER)
        lightingPassShader.link()

        // Set texture units IDs to Samplers 2D (for MRT)
        lightingPassShader.use()
        lightingPassShader.setUniformInt("gPos", 0)
        lightingPassShader.setUniformInt("gNor", 1)
        lightingPassShader.setUniformInt("gCol", 2)
    }

    override fun start() {
        super.start()
    }

    override fun update(deltaTime: Float) {
        if (!isStarted || !isAttachedToWindow || cameraNodes.size == 0) return
        Debug.logd(TAG, "update (deltaTime=$deltaTime)")

//        Debug.logi(TAG, "renderNodes=${renderNodes.size}")
//        Debug.logi(TAG, "cameraNodes=${cameraNodes.size}")
//        Debug.logi(TAG, "lightNodes=${lightNodes.size}")

        defaultBuffer.bind()
        defaultBuffer.clear()

        // --------------------------------------------- GEOMETRY PASS --------------------------------------------- //
        gBuffer.bind()
        gBuffer.clear()
        glEnable(GL_DEPTH_TEST)
        glActiveTexture(GL_TEXTURE2)

        val cameraNode = cameraNodes.find { it.cameraComponent.isActive }
            ?: throw RuntimeException("Can't find any active camera!")

        val projectionMat = cameraNode.cameraComponent.projectionMat
            ?: throw RuntimeException("Camera's projection matrix is null!")

        val viewMat = cameraNode.transformComponent.rotatable.rotation.toMat4()
            .translate(-cameraNode.transformComponent.movable.position)

        // Draw terrain and default models
        for (renderNode in renderNodes) {
            val transformationMat = Mat4(1f)
                .translate(renderNode.transformComponent.movable.position)
                .times(renderNode.transformComponent.rotatable.rotation.toMat4())
                .scale_(renderNode.transformComponent.scalable.scale)

            when (renderNode.modelComponent.model) {
                is Terrain -> {
                    Terrain.shaderProgram.use()
                    Terrain.shaderProgram.setUniformMat4f("m", transformationMat)
                    Terrain.shaderProgram.setUniformMat4f("v", viewMat)
                    Terrain.shaderProgram.setUniformMat4f("p", projectionMat)

                    renderNode.modelComponent.model.bind()
                    glDrawElements(
                        GL_TRIANGLES,
                        renderNode.modelComponent.model.mesh.indices!!.size,
                        GL_UNSIGNED_INT,
                        0
                    )
                }
                is ModelDefault -> {
                    ModelDefault.shaderProgram.use()
                    ModelDefault.shaderProgram.setUniformMat4f("m", transformationMat)
                    ModelDefault.shaderProgram.setUniformMat4f("v", viewMat)
                    ModelDefault.shaderProgram.setUniformMat4f("p", projectionMat)

                    val modelNormalMat = glm.transpose(glm.inverse(transformationMat.toMat3()))
                    ModelDefault.shaderProgram.setUniformMat3f("normalMatrix", modelNormalMat)

                    renderNode.modelComponent.model.bind()
                    renderNode.modelComponent.model.texture!!.bind()
                    glDrawElements(
                        GL_TRIANGLES,
                        renderNode.modelComponent.model.mesh.indices!!.size,
                        GL_UNSIGNED_INT,
                        0
                    )
                }
            }
        }

        // --------------------------------------------- LIGHTING PASS --------------------------------------------- //
        defaultBuffer.bind()
        defaultBuffer.clear()
        gBuffer.activateTextures()

        // var noPointLights = 0
        // var noSpotLights = 0
        // lightNodes.forEach {
        //     when (it.lightComponent.light) {
        //         is LightPoint -> noPointLights++
        //         is LightSpot -> noSpotLights++
        //     }
        // }
        val noPointLights = lightNodes.count { it.lightComponent.light is LightPoint }
        val noSpotLights = lightNodes.count { it.lightComponent.light is LightSpot }

        lightingPassShader.use()
        lightingPassShader.setUniformInt("noPointLights", noPointLights)
        lightingPassShader.setUniformInt("noSpotLights", noSpotLights)

        for (lightNode in lightNodes) {
            lightNode.lightComponent.light.apply(lightingPassShader)
        }

        glDisable(GL_DEPTH_TEST)
        defaultBuffer.draw()
        glEnable(GL_DEPTH_TEST)


        // ------------------------------------------- FORWARD RENDERING ------------------------------------------- //
        // Copy depth information from gBuffer to defaultBuffer
        gBuffer.copyDepthTo(defaultBuffer)
        defaultBuffer.bind()

        // Draw no light models
        for (renderNode in renderNodes) {
            val transformationMat = Mat4(1f)
                .translate(renderNode.transformComponent.movable.position)
                .times(renderNode.transformComponent.rotatable.rotation.toMat4())
                .scale_(renderNode.transformComponent.scalable.scale)

            when (renderNode.modelComponent.model) {
                is ModelNoLight -> {
                    ModelNoLight.shaderProgram.use()
                    ModelNoLight.shaderProgram.setUniformMat4f("m", transformationMat)
                    ModelNoLight.shaderProgram.setUniformMat4f("v", viewMat)
                    ModelNoLight.shaderProgram.setUniformMat4f("p", projectionMat)
                    renderNode.modelComponent.model.bind()
                    renderNode.modelComponent.model.texture!!.bind()
                    glDrawElements(
                        GL_TRIANGLES,
                        renderNode.modelComponent.model.mesh.indices!!.size,
                        GL_UNSIGNED_INT,
                        0
                    )
                }
            }
        }

        // Draw bounding boxes
        for (renderNode in renderNodes) {
            val oobb = when(renderNode.modelComponent.model) {
                is ModelNoLight -> { ((renderNode.modelComponent.model) as ModelNoLight).orientedBoundingBox }
                is ModelDefault -> { ((renderNode.modelComponent.model) as ModelDefault).orientedBoundingBox }
                else -> null
            }

            val aabb = when (renderNode.modelComponent.model) {
                is ModelNoLight -> { ((renderNode.modelComponent.model) as ModelNoLight).axisAlignedBoundingBox }
                is ModelDefault -> { ((renderNode.modelComponent.model) as ModelDefault).axisAlignedBoundingBox }
                else -> null
            }

            if (oobb == null || aabb == null) continue

            val transformationMat = Mat4(1f)
                .translate(renderNode.transformComponent.movable.position)
                .times(renderNode.transformComponent.rotatable.rotation.toMat4())
                .scale_(renderNode.transformComponent.scalable.scale)

            ModelNoLight.shaderProgram.use()
            ModelNoLight.shaderProgram.setUniformMat4f("m", Mat4(1f))

            aabb.update(transformationMat) // TODO: Update in CollisionSystem
            aabb.bind()
            aabb.texture!!.bind()
            glDrawElements(GL_LINES, aabb.primaryMesh.indices!!.size, GL_UNSIGNED_INT, 0)

            ModelNoLight.shaderProgram.setUniformMat4f("m", transformationMat)
            oobb.update(transformationMat) // TODO: Update in CollisionSystem
            oobb.bind()
            oobb.texture!!.bind()
            glDrawElements(GL_LINES, oobb.primaryMesh.indices!!.size, GL_UNSIGNED_INT, 0)
        }

        // Draw skybox
        // world.skybox?.let { skybox ->
        //     Skybox.shaderProgram.use()
        //     Skybox.shaderProgram.setUniformMat4f("v", camera.viewMat.toMat3().toMat4())
        //     Skybox.shaderProgram.setUniformMat4f("p", this.projectionMat)
        //     skybox.bind()
        //     glBindTexture(GL_TEXTURE_CUBE_MAP, skybox.textureID)
        //     glDepthFunc(GL_LEQUAL)
        //     glDrawArrays(GL_TRIANGLES, 0, 36)
        //     glDepthFunc(GL_LESS)
        // }

        glfwSwapBuffers(window)
    }

    override fun stop() {
        super.stop()
    }
}