package utils

import org.lwjgl.opengl.GL33.*

object OpenGLUtils {
    private val TAG: String = this::class.java.name

    fun getCurrentTextureID() = glGetInteger(GL_TEXTURE_BINDING_2D)

    fun readOpenGLError(): String? {
        var e = glGetError()
        val sb = StringBuilder()

        while (e != GL_NO_ERROR) {
            sb.append("OpenGL error: $e\n")
            e = glGetError()
        }

        return if (sb.isEmpty()) {
            null
        } else {
            sb.toString()
        }
    }
}