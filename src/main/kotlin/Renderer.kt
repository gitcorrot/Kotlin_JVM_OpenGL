import glm_.glm
import models.ModelDefault
import models.ModelNoLight
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


        // 1. Apply all light sources
        ModelDefault.shaderProgram.use()
        for (lightSource in world.lightSources) {
            lightSource.apply(ModelDefault.shaderProgram)
        }

        // 2. Draw terrain
        // TODO: Implement terrain

        // 3. Draw all models
        // 3.1 Default models
        ModelDefault.shaderProgram.setUniformMat4f("v", camera.viewMat)
        ModelDefault.shaderProgram.setUniformMat4f("p", this.projectionMat)
        for (model in world.modelsDefault) {
            model.bind()
            model.texture.bind()

            val modelTransMat = model.getTransformationMat()
            ModelDefault.shaderProgram.setUniformMat4f("m", modelTransMat)

            val modelNormalMat = glm.transpose(glm.inverse(modelTransMat.toMat3()))
            ModelDefault.shaderProgram.setUniformMat3f("normalMatrix", modelNormalMat)

            ModelDefault.shaderProgram.setUniformVec3f("cameraPosition", camera.position)

            glDrawElements(GL_TRIANGLES, model.getIndicesCount(), GL_UNSIGNED_INT, 0)
        }

        // 3.2 No light models
        ModelNoLight.shaderProgram.use()
        ModelNoLight.shaderProgram.setUniformMat4f("v", camera.viewMat)
        ModelNoLight.shaderProgram.setUniformMat4f("p", this.projectionMat)
        for (modelNoLight in world.modelsNoLight) {
            modelNoLight.bind()
            modelNoLight.texture.bind()

            ModelNoLight.shaderProgram.setUniformMat4f("m", modelNoLight.getTransformationMat())

            glDrawElements(GL_TRIANGLES, modelNoLight.getIndicesCount(), GL_UNSIGNED_INT, 0)
        }

        // 4. Draw skybox
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
