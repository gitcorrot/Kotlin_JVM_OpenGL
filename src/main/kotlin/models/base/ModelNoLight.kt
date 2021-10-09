package models.base

import ShaderProgram
import Texture
import data.Mesh
import org.lwjgl.opengl.GL33.*
import utils.Debug
import utils.ResourcesUtils

class ModelNoLight(
    mesh: Mesh,
    texture: Texture
) : Model(mesh, texture) {
    companion object {
        val shaderProgram = ShaderProgram()

        private const val VERTEX_SIZE = 5

        private const val vertexShaderPath = "model_no_light_vertex_shader.glsl"
        private const val fragmentShaderPath = "model_no_light_fragment_shader.glsl"

        init {
            val vertexShaderString = ResourcesUtils.readShader(vertexShaderPath)
            val fragmentShaderString = ResourcesUtils.readShader(fragmentShaderPath)
            shaderProgram.createShader(vertexShaderString, GL_VERTEX_SHADER)
            shaderProgram.createShader(fragmentShaderString, GL_FRAGMENT_SHADER)
            shaderProgram.link()
            shaderProgram.use()

            // Shader's sampler2d belong to texture unit 2
            shaderProgram.setUniformInt("colorPaletteTexture", 2)
        }
    }

    private val TAG: String = this::class.java.name

    init {
        this.vao = glGenVertexArrays()
        this.vbo = glGenBuffers()
        this.ebo = glGenBuffers()

        uploadVertices(mesh)
        uploadIndices(mesh)

        // 3 Float vertex coordinates
        glVertexAttribPointer(0, 3, GL_FLOAT, false, VERTEX_SIZE * Float.SIZE_BYTES, 0)
        glEnableVertexAttribArray(0)
        // 2 Float vertex texture coordinates
        glVertexAttribPointer(1, 2, GL_FLOAT, false, VERTEX_SIZE * Float.SIZE_BYTES, 3L * Float.SIZE_BYTES)
        glEnableVertexAttribArray(1)

        // Unbind VBO and VAO
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)

        Debug.logd(TAG, "ModelNoLight created!")
    }

    fun uploadVertices(mesh: Mesh) {
        super.uploadVertices(mesh, VERTEX_SIZE)
    }
}
