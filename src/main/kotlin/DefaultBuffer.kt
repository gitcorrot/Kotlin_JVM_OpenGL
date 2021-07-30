import models.base.Quad
import org.lwjgl.opengl.GL33.*

class DefaultBuffer(
    override val width: Int,
    override val height: Int
) : Framebuffer() {

    override var id = 0
    override val quad = Quad()

    override fun clear() {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
    }
}