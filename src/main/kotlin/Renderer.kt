import glm_.glm
import org.lwjgl.glfw.GLFW.glfwSwapBuffers
import org.lwjgl.opengl.GL33.*


class Renderer(
    private val window: Long,
    private val width: Int,
    private val height: Int
) {
    private val TAG: String = this::class.java.name


    private val fov: Float = glm.radians(60f)
    private val aspectRatio: Float = width / height.toFloat()
    private val zNear: Float = 0.1f
    private val zFar: Float = 1000.0f

    private val projectionMat = glm.perspective(fov, aspectRatio, zNear, zFar)

    init {
        glEnable(GL_DEPTH_TEST)
        glClearColor(.1f, .8f, .8f, 0.0f)
    }

    fun render(world: World, camera: Camera) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // 1. Draw all light sources
        LightSource.shaderProgram.use()
        LightSource.shaderProgram.setUniformMat4f("v", camera.viewMat)
        LightSource.shaderProgram.setUniformMat4f("p", this.projectionMat)
        for (lightSource in world.lightSources) {
            lightSource.bind()
            lightSource.texture.bind()

            LightSource.shaderProgram.setUniformMat4f("m", lightSource.getTransformationMat())

            glDrawElements(GL_TRIANGLES, lightSource.getIndicesCount(), GL_UNSIGNED_INT, 0)
        }

        // 2. Draw terrain
        // TODO: Implement terrain

        // 3. Draw all models
        DefaultModel.shaderProgram.use()
        DefaultModel.shaderProgram.setUniformMat4f("v", camera.viewMat)
        DefaultModel.shaderProgram.setUniformMat4f("p", this.projectionMat)
        for (model in world.defaultModels) {
            model.bind()
            model.texture.bind()

            val modelTransMat = model.getTransformationMat()
            DefaultModel.shaderProgram.setUniformMat4f("m", modelTransMat)

            val modelNormalMat = glm.transpose(glm.inverse(modelTransMat.toMat3()))
            DefaultModel.shaderProgram.setUniformMat3f("normalMatrix", modelNormalMat)

            DefaultModel.shaderProgram.setUniformVec3f("cameraPosition", camera.position)

            // TODO: implement multiple light sources
            DefaultModel.shaderProgram.setUniformVec3f("lightPosition", world.lightSources[0].tranformation.translation)

            glDrawElements(GL_TRIANGLES, model.getIndicesCount(), GL_UNSIGNED_INT, 0)
        }

        // Draw skybox at the end
        world.skybox?.let { skybox ->
            Skybox.shaderProgram.use()
            skybox.bind()
            glBindTexture(GL_TEXTURE_CUBE_MAP, skybox.textureID)
            Skybox.shaderProgram.setUniformMat4f("v", camera.viewMat.toMat3().toMat4())
            Skybox.shaderProgram.setUniformMat4f("p", this.projectionMat)
            // TODO: Refactor skybox texture (Texture as open class and derive for multiple types of texture)
            glDepthFunc(GL_LEQUAL)
            glDrawArrays(GL_TRIANGLES, 0, 36)
            glDepthFunc(GL_LESS);
        }

        glfwSwapBuffers(window)
    }
}
