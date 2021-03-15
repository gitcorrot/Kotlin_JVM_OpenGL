import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer

object ResourcesUtils {
    private val TAG: String = this::class.java.name

    fun loadStringFromFile(path: String): String {
        return this.javaClass.getResource(path).readText()
    }

    @Throws
    fun loadImage(path: String): Image {
        val image: ByteBuffer
        val imageWidth: Int
        val imageHeight: Int

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