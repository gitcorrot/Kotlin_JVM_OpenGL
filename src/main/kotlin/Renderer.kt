import glm_.glm
import models.base.ModelDefault
import models.base.ModelNoLight
import models.base.Terrain
import models.base.Quad
import org.lwjgl.glfw.GLFW.glfwSwapBuffers
import org.lwjgl.opengl.GL33.*
import org.lwjgl.system.MemoryUtil
import utils.Debug
import utils.ResourcesUtils


class Renderer(
    private val window: Long,
    private val width: Int,
    private val height: Int
) {
    private val TAG: String = this::class.java.name


    private val lightingPassShader = ShaderProgram()

    private val gBuffer = glGenFramebuffers()
    private val gPosition = glGenTextures()
    private val gNormal = glGenTextures()
    private val gColor = glGenTextures()

    private val fov: Float = glm.radians(60f)
    private val aspectRatio: Float = width / height.toFloat()
    private val zNear: Float = 0.1f
    private val zFar: Float = 1000.0f
    private val projectionMat = glm.perspective(fov, aspectRatio, zNear, zFar)

    private val quad = Quad()

    init {
        initLightingPassShader()
        initGBuffer()
        initDepthBuffer()

        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LESS)
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

    private fun initGBuffer() {
        glBindFramebuffer(GL_FRAMEBUFFER, gBuffer)

        // Position
        glBindTexture(GL_TEXTURE_2D, gPosition)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, WINDOW_WIDTH, WINDOW_HEIGHT, 0, GL_RGB, GL_FLOAT, MemoryUtil.NULL)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, gPosition, 0)

        // Normal
        glBindTexture(GL_TEXTURE_2D, gNormal)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, WINDOW_WIDTH, WINDOW_HEIGHT, 0, GL_RGB, GL_FLOAT, MemoryUtil.NULL)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, gNormal, 0)

        // Color
        glBindTexture(GL_TEXTURE_2D, gColor)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, WINDOW_WIDTH, WINDOW_HEIGHT, 0, GL_RGB, GL_FLOAT, MemoryUtil.NULL)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT2, GL_TEXTURE_2D, gColor, 0)

        val buffers = intArrayOf(GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2)
        glDrawBuffers(buffers)

    }

    private fun initDepthBuffer() {
        glBindFramebuffer(GL_FRAMEBUFFER, gBuffer)
        val rboDepth = glGenRenderbuffers()
        glBindRenderbuffer(GL_RENDERBUFFER, rboDepth)
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, WINDOW_WIDTH, WINDOW_HEIGHT)
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rboDepth)
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("Framebuffer not complete!")
        } else {
            Debug.logi(TAG, "Framebuffer complete")
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    fun render(world: World, camera: Camera) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        glEnable(GL_DEPTH_TEST)

        glBindFramebuffer(GL_FRAMEBUFFER, gBuffer)
        glClearColor(0f, 0f, 0f, 1f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // --------------------------------------------- GEOMETRY PASS --------------------------------------------- //
        // Draw terrain
        Terrain.shaderProgram.use()
        Terrain.shaderProgram.setUniformMat4f("v", camera.viewMat)
        Terrain.shaderProgram.setUniformMat4f("p", this.projectionMat)
        for (terrain in world.terrains) {
            val terrainTransMat = terrain.transformationMat
            Terrain.shaderProgram.setUniformMat4f("m", terrainTransMat)
            terrain.bind()
            glActiveTexture(GL_TEXTURE2)
            glDrawElements(GL_TRIANGLES, terrain.getIndicesCount(), GL_UNSIGNED_INT, 0)
        }
        // Draw default models
        ModelDefault.shaderProgram.use()
        ModelDefault.shaderProgram.setUniformMat4f("v", camera.viewMat)
        ModelDefault.shaderProgram.setUniformMat4f("p", this.projectionMat)
        for (model in world.modelsDefault) {
            model.bind()
            glActiveTexture(GL_TEXTURE2)
            model.texture.bind()

            val modelTransMat = model.transformationMat
            val modelNormalMat = glm.transpose(glm.inverse(modelTransMat.toMat3()))
            ModelDefault.shaderProgram.setUniformMat4f("m", modelTransMat)
            ModelDefault.shaderProgram.setUniformMat3f("normalMatrix", modelNormalMat)

            glDrawElements(GL_TRIANGLES, model.getIndicesCount(), GL_UNSIGNED_INT, 0)
        }

        // --------------------------------------------- LIGHTING PASS --------------------------------------------- //
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        lightingPassShader.use()
        glDisable(GL_DEPTH_TEST)
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, gPosition)
        glActiveTexture(GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, gNormal)
        glActiveTexture(GL_TEXTURE2)
        glBindTexture(GL_TEXTURE_2D, gColor)

        lightingPassShader.setUniformInt("noPointLights", 2)
        lightingPassShader.setUniformInt("noSpotLights", 1)

        for (lightSource in world.lightSources) {
            lightSource.apply(lightingPassShader)
        }

        quad.draw()

        // Copy gBuffer depth information to current frameBuffer (0)
        glEnable(GL_DEPTH_TEST)
        glBindFramebuffer(GL_READ_FRAMEBUFFER, gBuffer) // from
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0) // to
        glBlitFramebuffer(
            0, 0, WINDOW_WIDTH, WINDOW_HEIGHT,
            0, 0, WINDOW_WIDTH, WINDOW_HEIGHT,
            GL_DEPTH_BUFFER_BIT, GL_NEAREST
        )
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        // Draw no light models
        ModelNoLight.shaderProgram.use()
        ModelNoLight.shaderProgram.setUniformMat4f("v", camera.viewMat)
        ModelNoLight.shaderProgram.setUniformMat4f("p", this.projectionMat)
        for (modelNoLight in world.modelsNoLight) {
            modelNoLight.bind()
            modelNoLight.texture.bind()

            ModelNoLight.shaderProgram.setUniformMat4f("m", modelNoLight.transformationMat)

            glDrawElements(GL_TRIANGLES, modelNoLight.getIndicesCount(), GL_UNSIGNED_INT, 0)
        }

        // Draw skybox
        world.skybox?.let { skybox ->
            Skybox.shaderProgram.use()
            skybox.bind()
            glBindTexture(GL_TEXTURE_CUBE_MAP, skybox.textureID)
            Skybox.shaderProgram.setUniformMat4f("v", camera.viewMat.toMat3().toMat4())
            Skybox.shaderProgram.setUniformMat4f("p", this.projectionMat)
            // TODO: Refactor skybox texture (Texture as open class and derive for multiple types of texture)
            glDepthFunc(GL_LEQUAL)
            glDrawArrays(GL_TRIANGLES, 0, 36)
            glDepthFunc(GL_LESS)
        }

        glfwSwapBuffers(window)
    }
}
