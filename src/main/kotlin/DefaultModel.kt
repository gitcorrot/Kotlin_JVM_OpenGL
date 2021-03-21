import model.Mesh
import org.lwjgl.opengl.GL33.*
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer
import java.nio.IntBuffer

class DefaultModel(mesh: Mesh) : Model(mesh) {
    companion object {
        val TAG: String = this::class.java.name
        val shaderProgram = ShaderProgram()

        init {
            val vertexShaderString = ResourcesUtils.loadStringFromFile("Shaders/model_vertex_shader.glsl")
            val fragmentShaderString = ResourcesUtils.loadStringFromFile("Shaders/model_fragment_shader.glsl")
            shaderProgram.createShader(vertexShaderString, GL_VERTEX_SHADER)
            shaderProgram.createShader(fragmentShaderString, GL_FRAGMENT_SHADER)
            shaderProgram.link()
            shaderProgram.use()
        }
    }


    override fun create() {
        super.create()

        val verticesBuffer: FloatBuffer = MemoryUtil.memAllocFloat(mesh.vertices.size * 8) // each vertex has 8 floats
        for (v in mesh.vertices) {
            verticesBuffer.put(v.convertToFloatArray())
        }
        verticesBuffer.flip() // flip resets position to 0

        val indicesBuffer: IntBuffer = MemoryUtil.memAllocInt(mesh.indices.size)
        indicesBuffer
            .put(mesh.indices)
            .flip()

        this.vao = glGenVertexArrays()
        val vbo = glGenBuffers()
        val ebo = glGenBuffers()

        glBindVertexArray(vao)

        // for each vertex -> add vertex.getAsArray
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW)
        MemoryUtil.memFree(verticesBuffer)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW)
        MemoryUtil.memFree(indicesBuffer)

        // 3 Float vertex coordinates
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * 4, 0)
        glEnableVertexAttribArray(0)
        // 3 Float vertex normals
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * 4, 3 * 4)
        glEnableVertexAttribArray(1)
        // 2 Float vertex texture coordinates
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * 4, 6 * 4)
        glEnableVertexAttribArray(2)

        // Unbind VBO and VAO
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)

        Debug.logd(TAG, "Model created!")
    }
}
