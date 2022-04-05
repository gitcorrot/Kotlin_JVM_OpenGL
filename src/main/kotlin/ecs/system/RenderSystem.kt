package ecs.system

import AppSettings
import CameraNodes
import CollisionNodes
import DefaultBuffer
import Framebuffer
import GBuffer
import LightNodes
import RenderNodes
import ShaderProgram
import Skybox
import ecs.component.CollisionComponent
import ecs.node.RenderNode
import glm_.glm
import glm_.mat4x4.Mat4
import light.LightPoint
import light.LightSpot
import models.base.ModelDefault
import models.base.ModelNoLight
import models.base.Terrain
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lwjgl.glfw.GLFW.glfwSwapBuffers
import org.lwjgl.opengl.GL33.*
import ui.view.LoadingView
import utils.Debug
import utils.OpenGLUtils.getWindowSize
import utils.ResourcesUtils

class RenderSystem : BaseSystem(), KoinComponent {
    companion object {
        private val TAG: String = this::class.java.name
    }

    private val cameraNodes by inject<CameraNodes>()
    private val lightNodes by inject<LightNodes>()
    private val renderNodes by inject<RenderNodes>()
    private val collisionNodes by inject<CollisionNodes>()

    private val appSettings by inject<AppSettings>()

    private var window: Long = -1
    private var isAttachedToWindow = false

    private val lightingPassShader = ShaderProgram()

    private lateinit var gBuffer: GBuffer
    private lateinit var defaultBuffer: Framebuffer

    private var windowWidth: Int = 0
    private var windowHeight: Int = 0
    private var aspectRatio: Float = 0f

    private var loadingView: LoadingView? = null
    private var drawCalls = 0
    private val skybox = Skybox() // TODO: Move it to some component (???)

    init {
        initLightingPassShader()
        glDepthFunc(GL_LESS)
    }

    fun attachToWindow(window: Long) {
        this.window = window
        isAttachedToWindow = true

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

    override fun update(deltaTime: Float) {
        if (!isStarted || !isAttachedToWindow || cameraNodes.size == 0) {
            loadingView?.let {
                defaultBuffer.bind()
                defaultBuffer.clear()
                it.render()
                glfwSwapBuffers(window)
            }
            return
        }

//        Debug.logd(TAG, "update (deltaTime=$deltaTime)")

//        Debug.logi(TAG, "renderNodes=${renderNodes.size}")
//        Debug.logi(TAG, "cameraNodes=${cameraNodes.size}")
//        Debug.logi(TAG, "lightNodes=${lightNodes.size}")

        defaultBuffer.bind()
        defaultBuffer.clear()

        drawCalls = 0

        val cameraNode = cameraNodes.find { it.cameraComponent.isActive }
            ?: throw RuntimeException("Can't find any active camera!")

        val projectionMat = cameraNode.cameraComponent.projectionMat
            ?: throw RuntimeException("Camera's projection matrix is null!")

        val viewMat = cameraNode.transformComponent.rotatable.rotation.toMat4()
            .translate(-cameraNode.transformComponent.movable.position)

        // --------------------------------------------- GEOMETRY PASS --------------------------------------------- //
        gBuffer.bind()
        gBuffer.clear()
        glEnable(GL_DEPTH_TEST)
        glActiveTexture(GL_TEXTURE2)

        for (renderNode in renderNodes) {
            when (renderNode.modelComponent.model) {
                is Terrain -> {
                    drawTerrain(viewMat, projectionMat, renderNode, Terrain.shaderProgram)
                }
                is ModelDefault -> {
                    drawModelDefault(viewMat, projectionMat, renderNode)
                }
            }
        }

        // --------------------------------------------- LIGHTING PASS --------------------------------------------- //
        defaultBuffer.bind()
        defaultBuffer.clear()
        gBuffer.activateTextures()

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
            when (renderNode.modelComponent.model) {
                is ModelNoLight -> {
                    drawModelNoLight(viewMat, projectionMat, renderNode)
                }
            }
        }

        if (appSettings.drawBoundingBoxes) {
            for (collisionNode in collisionNodes) {
                drawBoundingBox(collisionNode.collisionComponent)
            }
        }

        if (appSettings.drawTerrainNormals) {
            for (renderNode in renderNodes) {
                when (renderNode.modelComponent.model) {
                    is Terrain -> {
                        drawTerrain(viewMat, projectionMat, renderNode, Terrain.normalDebugShaderProgram)
                    }
                }
            }
        }

        drawSkybox(viewMat, projectionMat)

        glfwSwapBuffers(window)

        Debug.logd(TAG, "Draw calls: $drawCalls")
    }

    private fun drawTerrain(
        viewMat: Mat4,
        projectionMat: Mat4,
        renderNode: RenderNode,
        shaderProgram: ShaderProgram
    ) {
        val transformationMat = renderNode.transformComponent.getTransformationMat()
        val model = renderNode.modelComponent.model

        shaderProgram.use()
        shaderProgram.setUniformMat4f("m", transformationMat)
        shaderProgram.setUniformMat4f("v", viewMat)
        shaderProgram.setUniformMat4f("p", projectionMat)

        model.bind()
        glDrawElements(GL_TRIANGLES, model.mesh.indices!!.size, GL_UNSIGNED_INT, 0)
        drawCalls++
    }

    private fun drawModelNoLight(
        viewMat: Mat4,
        projectionMat: Mat4,
        renderNode: RenderNode,
    ) {
        val transformationMat = renderNode.transformComponent.getTransformationMat()
        val model = renderNode.modelComponent.model

        ModelNoLight.shaderProgram.use()
        ModelNoLight.shaderProgram.setUniformMat4f("m", transformationMat)
        ModelNoLight.shaderProgram.setUniformMat4f("v", viewMat)
        ModelNoLight.shaderProgram.setUniformMat4f("p", projectionMat)

        model.bind()
        model.texture!!.bind()
        glDrawElements(GL_TRIANGLES, model.mesh.indices!!.size, GL_UNSIGNED_INT, 0)
        drawCalls++
    }

    private fun drawModelDefault(
        viewMat: Mat4,
        projectionMat: Mat4,
        renderNode: RenderNode,
    ) {
        val transformationMat = renderNode.transformComponent.getTransformationMat()
        val model = renderNode.modelComponent.model

        ModelDefault.shaderProgram.use()
        ModelDefault.shaderProgram.setUniformMat4f("m", transformationMat)
        ModelDefault.shaderProgram.setUniformMat4f("v", viewMat)
        ModelDefault.shaderProgram.setUniformMat4f("p", projectionMat)

        val modelNormalMat = glm.transpose(glm.inverse(transformationMat.toMat3()))
        ModelDefault.shaderProgram.setUniformMat3f("normalMatrix", modelNormalMat)

        model.bind()
        model.texture!!.bind()
        glDrawElements(GL_TRIANGLES, model.mesh.indices!!.size, GL_UNSIGNED_INT, 0)
        drawCalls++
    }

    private fun drawBoundingBox(collisionComponent: CollisionComponent) {
        val model = collisionComponent.boundingBoxModel

        ModelNoLight.shaderProgram.use()
        ModelNoLight.shaderProgram.setUniformMat4f("m", Mat4(1f))

        model.bind()
        model.texture!!.bind()
        glDrawElements(GL_LINES, model.mesh.indices?.size!!, GL_UNSIGNED_INT, 0)
        drawCalls++

    }

    private fun drawSkybox(
        viewMat: Mat4,
        projectionMat: Mat4
    ) {
        val skyboxViewMat = viewMat.apply {
            this[3][0] = 0f
            this[3][1] = 0f
            this[3][2] = 0f
            this[3][3] = 0f
            this[2][3] = 0f
            this[1][3] = 0f
            this[0][3] = 0f
        }

        Skybox.shaderProgram.use()
//      Skybox.shaderProgram.setUniformMat4f("v", viewMat.toMat3().toMat4())
        Skybox.shaderProgram.setUniformMat4f("v", skyboxViewMat)
        Skybox.shaderProgram.setUniformMat4f("p", projectionMat)

        skybox.bind()
        glBindTexture(GL_TEXTURE_CUBE_MAP, skybox.textureID)
        glDepthFunc(GL_LEQUAL)
        glDrawArrays(GL_TRIANGLES, 0, 36)
        glDepthFunc(GL_LESS)
        drawCalls++
    }
}