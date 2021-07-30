import glm_.glm
import glm_.mat4x4.Mat4
import org.lwjgl.glfw.GLFW.glfwSwapBuffers
import org.lwjgl.opengl.GL33.*
import ui.LoadingView
import utils.OpenGLUtils.getWindowSize
import utils.ResourcesUtils


class Renderer(
    private val window: Long
) {
    companion object {
        private val TAG: String = this::class.java.name

        const val FOV_DEG = 60f
        const val Z_NEAR = 0.1f
        const val Z_FAR = 1000.0f
    }

    private val lightingPassShader = ShaderProgram()

    private val gBuffer: GBuffer
    private val defaultBuffer: Framebuffer

    private val projectionMat: Mat4
    private val windowWidth: Int
    private val windowHeight: Int
    private val aspectRatio: Float

    private val loadingView: LoadingView

    init {
        with(getWindowSize(window)) {
            windowWidth = x.toInt()
            windowHeight = y.toInt()
            aspectRatio = x / y
        }

        projectionMat = glm.perspective(glm.radians(FOV_DEG), aspectRatio, Z_NEAR, Z_FAR)

        loadingView = LoadingView(aspectRatio)

        initLightingPassShader()

        gBuffer = GBuffer(windowWidth, windowHeight)
        defaultBuffer = DefaultBuffer(windowWidth, windowHeight)

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


    fun render(world: World?, camera: Camera) {
        defaultBuffer.bind()
        defaultBuffer.clear()

        if (world == null) {
            loadingView.render()
            glfwSwapBuffers(window)
            return
        }

        // --------------------------------------------- GEOMETRY PASS --------------------------------------------- //
        gBuffer.bind()
        gBuffer.clear()
        glEnable(GL_DEPTH_TEST)
        glActiveTexture(GL_TEXTURE2)

        // Draw terrain
        for (terrain in world.terrains) {
            terrain.draw(camera.viewMat, projectionMat)
        }

        // Draw default models
        for (model in world.modelsDefault) {
            model.draw(camera.viewMat, projectionMat)
        }

        // --------------------------------------------- LIGHTING PASS --------------------------------------------- //
        defaultBuffer.bind()
        defaultBuffer.clear()
        gBuffer.activateTextures()

        lightingPassShader.use()
        lightingPassShader.setUniformInt("noPointLights", world.noPointLights)
        lightingPassShader.setUniformInt("noSpotLights", world.noSpotLights)

        for (lightSource in world.lightSources) {
            lightSource.apply(lightingPassShader)
        }

        glDisable(GL_DEPTH_TEST)
        defaultBuffer.draw()
        glEnable(GL_DEPTH_TEST)

        // ------------------------------------------- FORWARD RENDERING ------------------------------------------- //
        // Copy depth information from gBuffer to defaultBuffer
        gBuffer.copyDepthTo(defaultBuffer)
        defaultBuffer.bind()

        // Draw no light models
        for (modelNoLight in world.modelsNoLight) {
            modelNoLight.draw(camera.viewMat, projectionMat)
        }

        // Draw bounding boxes
        for (model in world.modelsNoLight) {
            model.drawBoundingBoxes()
        }
        for (model in world.modelsDefault) {
            model.drawBoundingBoxes()
        }

        // Draw skybox
        world.skybox?.let { skybox ->
            Skybox.shaderProgram.use()
            skybox.bind()
            glBindTexture(GL_TEXTURE_CUBE_MAP, skybox.textureID)
            Skybox.shaderProgram.setUniformMat4f("v", camera.viewMat.toMat3().toMat4())
            Skybox.shaderProgram.setUniformMat4f("p", this.projectionMat)
            glDepthFunc(GL_LEQUAL)
            glDrawArrays(GL_TRIANGLES, 0, 36)
            glDepthFunc(GL_LESS)
        }

        glfwSwapBuffers(window)
    }
}
