import org.lwjgl.opengl.GL33.*
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryUtil
import utils.Debug
import utils.ResourcesUtils
import utils.ResourcesUtils.TEXTURES_PATH
import java.nio.FloatBuffer

private val skyboxVertices = floatArrayOf(
    -1.0f,  1.0f, -1.0f,
    -1.0f, -1.0f, -1.0f,
    1.0f, -1.0f, -1.0f,
    1.0f, -1.0f, -1.0f,
    1.0f,  1.0f, -1.0f,
    -1.0f,  1.0f, -1.0f,

    -1.0f, -1.0f,  1.0f,
    -1.0f, -1.0f, -1.0f,
    -1.0f,  1.0f, -1.0f,
    -1.0f,  1.0f, -1.0f,
    -1.0f,  1.0f,  1.0f,
    -1.0f, -1.0f,  1.0f,

    1.0f, -1.0f, -1.0f,
    1.0f, -1.0f,  1.0f,
    1.0f,  1.0f,  1.0f,
    1.0f,  1.0f,  1.0f,
    1.0f,  1.0f, -1.0f,
    1.0f, -1.0f, -1.0f,

    -1.0f, -1.0f,  1.0f,
    -1.0f,  1.0f,  1.0f,
    1.0f,  1.0f,  1.0f,
    1.0f,  1.0f,  1.0f,
    1.0f, -1.0f,  1.0f,
    -1.0f, -1.0f,  1.0f,

    -1.0f,  1.0f, -1.0f,
    1.0f,  1.0f, -1.0f,
    1.0f,  1.0f,  1.0f,
    1.0f,  1.0f,  1.0f,
    -1.0f,  1.0f,  1.0f,
    -1.0f,  1.0f, -1.0f,

    -1.0f, -1.0f, -1.0f,
    -1.0f, -1.0f,  1.0f,
    1.0f, -1.0f, -1.0f,
    1.0f, -1.0f, -1.0f,
    -1.0f, -1.0f,  1.0f,
    1.0f, -1.0f,  1.0f
)

// TODO: Implement system for attaching proper textures in constructor/function
private val texturePaths = arrayOf(
    "$TEXTURES_PATH/skybox/right.png",
    "$TEXTURES_PATH/skybox/left.png",
    "$TEXTURES_PATH/skybox/top.png",
    "$TEXTURES_PATH/skybox/bottom.png",
    "$TEXTURES_PATH/skybox/front.png",
    "$TEXTURES_PATH/skybox/back.png"
)

class Skybox {
    companion object {
        val TAG: String = this::class.java.name
        val shaderProgram = ShaderProgram()

        init {
            val vertexShaderString = ResourcesUtils.readShader("skybox_vertex_shader.glsl")
            val fragmentShaderString = ResourcesUtils.readShader("skybox_fragment_shader.glsl")
            shaderProgram.createShader(vertexShaderString, GL_VERTEX_SHADER)
            shaderProgram.createShader(fragmentShaderString, GL_FRAGMENT_SHADER)
            shaderProgram.link()
            shaderProgram.use()
        }
    }

    var textureID = -1

    private var vao: Int = -1
        @Throws
        get() {
            if (field == -1)
                throw Exception("models.Base.Model VAO id not assigned!")
            return field
        }

    init {
        val verticesBuffer: FloatBuffer = MemoryUtil.memAllocFloat(skyboxVertices.size)
        verticesBuffer.put(skyboxVertices)
        verticesBuffer.flip() // flip resets position to 0

        this.vao = glGenVertexArrays()
        val vbo = glGenBuffers()

        glBindVertexArray(vao)

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW)
        MemoryUtil.memFree(verticesBuffer)

        // 3 Float vertex coordinates
        glVertexAttribPointer(0, 3, GL_FLOAT, true, 3 * 4, 0)
        glEnableVertexAttribArray(0)

        // Load cubemap
        textureID = loadCubemap()

        // Unbind VBO and VAO
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
    }

    private fun loadCubemap(): Int {
        val texture = glGenTextures()
        glBindTexture(GL_TEXTURE_CUBE_MAP, texture)


        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        for ((index, path) in texturePaths.withIndex()) {
            Debug.logd(TAG, "${index}:${path}")
            val img = ResourcesUtils.loadImage(path, false)
            glTexImage2D(
                GL_TEXTURE_CUBE_MAP_POSITIVE_X + index,
                0, GL_RGB, img.width, img.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, img.data
            )
            STBImage.stbi_image_free(img.data)
        }

        return texture
    }

    fun bind() {
        glBindVertexArray(vao)
    }

    fun cleanup() {
        glDeleteVertexArrays(vao)
    }
}