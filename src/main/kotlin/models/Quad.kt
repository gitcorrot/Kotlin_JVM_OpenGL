package models

import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer
import org.lwjgl.opengl.GL33.*

class Quad {

    private var vao: Int
    private var vbo: Int

    init {
        val quadVertices = floatArrayOf(
            // positions           // texture Coords
            -1.0f,  1.0f, 0.0f,    0.0f, 1.0f,
            -1.0f, -1.0f, 0.0f,    0.0f, 0.0f,
            1.0f,  1.0f, 0.0f,     1.0f, 1.0f,
            1.0f, -1.0f, 0.0f,     1.0f, 0.0f,
        )

        val verticesBuffer: FloatBuffer = MemoryUtil.memAllocFloat(quadVertices.size * 5)
        verticesBuffer.put(quadVertices)
        verticesBuffer.flip() // flip resets position to 0

        // setup plane VAO
        vao = glGenVertexArrays()
        vbo = glGenBuffers()
        glBindVertexArray(vao)
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW)
        // Positions
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * 4, 0)
        // Texture coordinates
        glEnableVertexAttribArray(1)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * 4, 3 * 4)
    }

    fun draw() {
        glBindVertexArray(vao)
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
        glBindVertexArray(0)
    }
}