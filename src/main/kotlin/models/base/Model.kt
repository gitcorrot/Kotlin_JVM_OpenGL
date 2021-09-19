package models.base

import Texture
import data.Mesh
import org.lwjgl.opengl.GL33.*
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer
import java.nio.IntBuffer


abstract class Model(
    val mesh: Mesh,
    val texture: Texture?
) {
    companion object {
        val TAG: String = this::class.java.name
    }

    var vao: Int = -1
        @Throws
        get() {
            if (field == -1)
                throw Exception("Model VAO id not assigned!")
            return field
        }

    var vbo: Int = -1
        @Throws
        get() {
            if (field == -1)
                throw Exception("Model VBO id not assigned!")
            return field
        }

    var ebo: Int = -1
        @Throws
        get() {
            if (field == -1)
                throw Exception("Model EBO id not assigned!")
            return field
        }


    fun uploadVertices(mesh: Mesh, vertexSize: Int) {
        val verticesBuffer: FloatBuffer =
            MemoryUtil.memAllocFloat(mesh.vertices.size * vertexSize)
        for (v in mesh.vertices) {
            verticesBuffer.put(v.convertToFloatArray())
        }
        verticesBuffer.flip() // flip resets position to 0

        glBindVertexArray(vao)
        // for each vertex -> add vertex.getAsArray
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW)
        MemoryUtil.memFree(verticesBuffer)
    }

    fun uploadIndices(mesh: Mesh) {

        val indicesBuffer: IntBuffer = MemoryUtil.memAllocInt(mesh.indices!!.size)
        indicesBuffer
            .put(mesh.indices)
            .flip()

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW)
        MemoryUtil.memFree(indicesBuffer)
    }

    fun bind() {
        glBindVertexArray(vao)
    }

    fun cleanup() {
        glDeleteVertexArrays(vao)
    }
}
