package utils

import data.Image
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import java.io.File
import java.nio.ByteBuffer

object ResourcesUtils {
    private val TAG: String = this::class.java.name

    const val RESOURCES_PATH = "src/main/resources"
    const val MODELS_PATH = "src/main/resources/Models"
    const val TEXTURES_PATH = "src/main/resources/Textures"
    const val SHADERS_PATH = "src/main/resources/Shaders"

    fun readShader(name: String): String {
        Debug.logd(TAG, "Reading $SHADERS_PATH/$name")
        return File("$SHADERS_PATH/$name").readText()
    }

    @Throws
    fun loadImage(path: String, flip: Boolean): Image {
        val image: ByteBuffer
        val imageWidth: Int
        val imageHeight: Int

        STBImage.stbi_set_flip_vertically_on_load(flip)

        MemoryStack.stackPush().use { stack ->
            val tmpChannels = stack.mallocInt(1)
            val tmpWidth = stack.mallocInt(1)
            val tmpHeight = stack.mallocInt(1)

            image = STBImage.stbi_load(
                path, tmpWidth, tmpHeight, tmpChannels, 0
            ) ?: throw Exception("Can't load image! Ensure that image is in proper resources folder.")

            imageWidth = tmpWidth.get()
            imageHeight = tmpHeight.get()

            Debug.logd(TAG, "Successfully loaded ${imageWidth}x${imageHeight}px, ${tmpChannels.get()} channels image")
        }

        return Image(image, imageWidth, imageHeight)
    }

}