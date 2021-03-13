import org.lwjgl.opengl.GL33.*
import org.lwjgl.stb.STBImage

class Texture {
    private val TAG: String = this::class.java.name

    private var textureID: Int = 0

    init {
        STBImage.stbi_set_flip_vertically_on_load(true)
    }

    fun createTexture(path: String) {
        textureID = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, textureID)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        val img = ResourcesUtils.loadImage(path)

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, img.width, img.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, img.data)
        glGenerateMipmap(textureID)
        STBImage.stbi_image_free(img.data)
    }

    fun bind() {
        glBindTexture(GL_TEXTURE_2D, textureID)
    }

    fun unbind() {
        glBindTexture(GL_TEXTURE_2D, 0)
    }

}