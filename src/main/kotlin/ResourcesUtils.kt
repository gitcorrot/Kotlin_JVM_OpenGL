import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack

object ResourcesUtils {
    private val TAG: String = this::class.java.name

    fun loadStringFromFile(path: String): String {
        return this.javaClass.getResource(path).readText()
    }

    @Throws
    fun loadImage(path: String): Image {
        val stack = MemoryStack.stackPush() // stack - we don't need to free it
        val tmpChannels = stack.mallocInt(1)
        val tmpWidth = stack.mallocInt(1)
        val tmpHeight = stack.mallocInt(1)

        val image = STBImage.stbi_load(
            path, tmpWidth, tmpHeight, tmpChannels, 0
        ) ?: throw Exception("Can't load image! Ensure that image is in proper resources folder.")

        val imageWidth = tmpWidth.get()
        val imageHeight = tmpHeight.get()

        Debug.logi(TAG, "Successfully loaded ${imageWidth}x${imageHeight}px, ${tmpChannels.get()} channels image")

        return Image(image, imageWidth, imageHeight)
    }

}