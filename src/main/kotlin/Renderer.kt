import glm_.glm
import org.lwjgl.glfw.GLFW.glfwPollEvents
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
    private val zFar: Float = 100.0f

    val projectionMat = glm.perspective(fov, aspectRatio, zNear, zFar)


    fun render(model: Model) {
        glClearColor(.2f, .7f, .7f, 0.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        model.bind()
        model.texture.bind()

        glDrawElements(GL_TRIANGLES, model.indicesSize, GL_UNSIGNED_INT, 0)

        glfwSwapBuffers(window)
        glfwPollEvents()
    }
}