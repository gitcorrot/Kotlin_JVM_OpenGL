import models.base.Quad
import org.lwjgl.opengl.GL33.*

abstract class Framebuffer {

    abstract var id: Int
    abstract val quad: Quad
    abstract val width: Int
    abstract val height: Int

    fun bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, id)
    }

    abstract fun clear()

    fun draw() {
        bind()
        quad.draw()
    }

    fun copyDepthTo(framebuffer: Framebuffer) {
        if (width != framebuffer.width || height != framebuffer.height) {
            throw RuntimeException("Frame buffers sizes have to match!")
        }

        glBindFramebuffer(GL_READ_FRAMEBUFFER, id) // from
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, framebuffer.id) // to
        glBlitFramebuffer(
            0, 0, width, height,
            0, 0, width, height,
            GL_DEPTH_BUFFER_BIT, GL_NEAREST
        )
    }
}