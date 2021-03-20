import glm_.glm
import org.lwjgl.glfw.GLFW.glfwSwapBuffers
import org.lwjgl.opengl.GL33.*


class Renderer(
    private val window: Long,
    private val width: Int,
    private val height: Int
) {
    private val TAG: String = this::class.java.name

    private val shaderProgram = ShaderProgram()

    private val fov: Float = glm.radians(60f)
    private val aspectRatio: Float = width / height.toFloat()
    private val zNear: Float = 0.1f
    private val zFar: Float = 1000.0f

    private val projectionMat = glm.perspective(fov, aspectRatio, zNear, zFar)

    init {
        val vertexShaderString = ResourcesUtils.loadStringFromFile("Shaders/vertex_shader.glsl")
        val fragmentShaderString = ResourcesUtils.loadStringFromFile("Shaders/fragment_shader.glsl")
        shaderProgram.createShader(vertexShaderString, GL_VERTEX_SHADER)
        shaderProgram.createShader(fragmentShaderString, GL_FRAGMENT_SHADER)
        shaderProgram.link()
        shaderProgram.use()

        glEnable(GL_DEPTH_TEST)
    }

    fun render(models: ArrayList<Model>, camera: Camera) {
        glClearColor(.1f, .8f, .8f, 0.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        for (model in models) {
            model.bind()
            model.texture.bind()

            val mvp = this.projectionMat
                .times(camera.viewMat)
                .times(model.transformation)

            shaderProgram.setUniformMat4f("mvp", mvp)

            glDrawElements(GL_TRIANGLES, model.getIndicesCount(), GL_UNSIGNED_INT, 0)
        }

        glfwSwapBuffers(window)
    }

    fun cleanup() {
        shaderProgram.cleanup()
    }
}