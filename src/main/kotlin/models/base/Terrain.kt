package models.base

import ShaderProgram
import data.Mesh
import glm_.mat4x4.Mat4
import org.lwjgl.opengl.GL33.*
import org.lwjgl.system.MemoryUtil
import utils.Debug
import utils.ResourcesUtils
import utils.TerrainUtils
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Terrain(
    val size: Int,
    val a: Float,
    val tileSize: Float
) : Model() {
    companion object {
        val TAG: String = this::class.java.name
        val shaderProgram = ShaderProgram()

        private const val vertexShaderPath = "terrain_vertex_shader.glsl"
        private const val geometryShaderPath = "terrain_geometry_shader.glsl"
        private const val fragmentShaderPath = "terrain_fragment_shader.glsl"

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

    override var mesh: Mesh? = null

    override fun addMesh(mesh: Mesh) {
        this.mesh = TerrainUtils.generateMesh(size, tileSize, a)
    }

    init {
        this.mesh = TerrainUtils.generateMesh(size, tileSize, a)
        this.create()
    }

    override fun create() {
        mesh?.let { mesh ->
            this.vao = glGenVertexArrays()
            this.vbo = glGenBuffers()
            this.ebo = glGenBuffers()

            val verticesBuffer: FloatBuffer =
                MemoryUtil.memAllocFloat(mesh.vertices.size * 9) // each vertex has 9 floats
            for (v in mesh.vertices) {
                verticesBuffer.put(v.convertToFloatArray())
            }
            verticesBuffer.flip() // flip resets position to 0

            val indicesBuffer: IntBuffer = MemoryUtil.memAllocInt(mesh.indices!!.size)
            indicesBuffer
                .put(mesh.indices)
                .flip()

            glBindVertexArray(vao)

            glBindBuffer(GL_ARRAY_BUFFER, vbo)
            glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW)
            MemoryUtil.memFree(verticesBuffer)

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW)
            MemoryUtil.memFree(indicesBuffer)

            // 3 Float vertex coordinates
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * 4, 0)
            glEnableVertexAttribArray(0)
            // 3 Float vertex colors
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * 4, 3 * 4)
            glEnableVertexAttribArray(1)

            // Unbind VBO and VAO
            glBindBuffer(GL_ARRAY_BUFFER, 0)
            glBindVertexArray(0)

            Debug.logd(TAG, "models.models.Base.Terrain created!")
        } ?: run {
            throw RuntimeException("Can't create Model without added Mesh!")
        }
    }

    fun draw(viewMat: Mat4, projectionMat: Mat4) {
        shaderProgram.use()
        shaderProgram.setUniformMat4f("m", transformationMat)
        shaderProgram.setUniformMat4f("v", viewMat)
        shaderProgram.setUniformMat4f("p", projectionMat)

        bind()
        glDrawElements(GL_TRIANGLES, getIndicesCount(), GL_UNSIGNED_INT, 0)
    }

    /**
     * Returns terrain height at given world coordinates (X, Z)
     */
    fun getHeightAt(x: Int, z: Int): Float {
        this.mesh?.let { mesh ->
            return mesh.vertices[((-z / tileSize.toInt()) * (size + 1)) + (x / tileSize.toInt())].position.y
//        return this.mesh.vertices[(z * (size + 1)) + x].position.y
        } ?: run {
            throw RuntimeException("Can't get terrain height without added Mesh!")
        }
    }
}
