import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL33.*
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Model {
    private val TAG: String = this::class.java.name

    private var vao: Int = -1
        @Throws
        get() {
            if (field == -1)
                throw Exception("Model VAO id not assigned!")
            return field
        }

    val texture = Texture()

    var indicesSize = -1

    // TODO: implement rotation
    var translation: Vec3 = Vec3(0f, 0f, 0f)

    // var rotation: Vec3 = Vec3(1f)
    var scale: Vec3 = Vec3(1f)

    val transformation: Mat4
        get() {
            return Mat4(1f)
                .translate_(translation)
//                .rotate(glm.radians(45f), Vec3(1f, 0f, 1f))
                .scale_(scale)
        }


    fun create(vertices: FloatArray, indices: IntArray) {

        // Create floats buffer and fill with vertices
        val verticesBuffer: FloatBuffer = MemoryUtil.memAllocFloat(vertices.size)
        verticesBuffer
            .put(vertices)
            .flip() // flip resets position to 0

        val indicesBuffer: IntBuffer = MemoryUtil.memAllocInt(indices.size)
        indicesBuffer
            .put(indices)
            .flip()

        this.indicesSize = indices.size

        this.vao = glGenVertexArrays()
        val vbo = glGenBuffers()
        val ebo = glGenBuffers()

        glBindVertexArray(vao)

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW)
        MemoryUtil.memFree(verticesBuffer)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW)
        MemoryUtil.memFree(indicesBuffer)

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * 4, 0)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * 4, 3 * 4)
        glEnableVertexAttribArray(1)

        // Unbind VBO and VAO
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)

        Debug.logd(TAG, "Model created!")
    }

    fun addTexture(path: String) {
        bind()
        texture.createTexture(path)
        Debug.logd(TAG, "Texture added to model!")
    }

    fun bind() {
        glBindVertexArray(vao)
    }

    fun cleanup() {
        glDeleteVertexArrays(vao)
    }
}
