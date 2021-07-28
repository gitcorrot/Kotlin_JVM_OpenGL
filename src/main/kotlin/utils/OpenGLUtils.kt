package utils

import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL33.*
import org.lwjgl.system.MemoryStack

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

    fun getWindowSize(window: Long): Vec2 {
        MemoryStack.stackPush().use { stack ->
            val tmpWidth = stack.mallocInt(1)
            val tmpHeight = stack.mallocInt(1)

            GLFW.glfwGetWindowSize(window, tmpWidth, tmpHeight)

            return Vec2(tmpWidth.get(), tmpHeight.get())
        }
    }
}