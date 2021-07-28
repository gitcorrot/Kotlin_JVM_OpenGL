package ui

import ShaderProgram
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL33.*
import org.lwjgl.system.MemoryUtil
import utils.ResourcesUtils
import java.nio.FloatBuffer

class LoadingView(
    private val aspectRatio: Float
) {
    companion object {
        val TAG: String = this::class.java.name
        val shaderProgram = ShaderProgram()

        private const val vertexShaderPath = "loading_vertex_shader.glsl"
        private const val geometryShaderPath = "loading_geometry_shader.glsl"
        private const val fragmentShaderPath = "loading_fragment_shader.glsl"

        init {
            val vertexShaderString = ResourcesUtils.readShader(vertexShaderPath)
            val geometryShaderString = ResourcesUtils.readShader(geometryShaderPath)
            val fragmentShaderString = ResourcesUtils.readShader(fragmentShaderPath)

            shaderProgram.createShader(vertexShaderString, GL_VERTEX_SHADER)
            shaderProgram.createShader(geometryShaderString, GL_GEOMETRY_SHADER)
            shaderProgram.createShader(fragmentShaderString, GL_FRAGMENT_SHADER)
            shaderProgram.link()
            shaderProgram.use()
        }
    }

    private var vao = -1
    private var vbo = -1

    init {
        vao = glGenVertexArrays()
        vbo = glGenBuffers()

        val verticesBuffer: FloatBuffer = MemoryUtil.memAllocFloat(2)
        verticesBuffer.put(floatArrayOf(0f, 0f))
        verticesBuffer.flip()
        glBindVertexArray(vao)
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.SIZE_BYTES, 0)
        glEnableVertexAttribArray(0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)

        glDisable(GL_DEPTH_TEST)
        glEnable(GL_PROGRAM_POINT_SIZE)
        glEnable(GL_POINT_SMOOTH)
    }

    fun render() {
        shaderProgram.use()
        glBindVertexArray(vao)
        shaderProgram.setUniformFloat("aspectRatio", aspectRatio)
        shaderProgram.setUniformFloat("time", GLFW.glfwGetTime().toFloat())
        glDrawArrays(GL_POINTS, 0, 1)
    }
}