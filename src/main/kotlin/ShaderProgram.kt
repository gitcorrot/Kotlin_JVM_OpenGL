import glm_.mat4x4.Mat4
import org.lwjgl.opengl.GL33.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.MemoryUtil.memAllocFloat
import org.lwjgl.system.MemoryUtil.memFree
import java.nio.FloatBuffer

class ShaderProgram {
    private val TAG: String = this::class.java.name

    private var programID: Int = 0
    private var vertexShaderID: Int = 0
    private var fragmentShaderID: Int = 0
    private var geometryShaderID: Int = 0

    init {
        programID = glCreateProgram()
    }

    @Throws
    fun createShader(shaderString: String, shaderType: Int) {
        val shaderID = glCreateShader(shaderType)
        glShaderSource(shaderID, shaderString)
        glCompileShader(shaderID)

        if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == 0) {
            throw Exception("model.Vertex Shader Info Log: ${glGetShaderInfoLog(shaderID)}")
        }

        when (shaderType) {
            GL_VERTEX_SHADER -> {
                vertexShaderID = shaderID
            }
            GL_FRAGMENT_SHADER -> {
                fragmentShaderID = shaderID
            }
            GL_GEOMETRY_SHADER -> {
                geometryShaderID = shaderID
            }
            else -> {
                throw Exception("Unknown shader type passed!")
            }
        }

        glAttachShader(programID, shaderID)

        Debug.logd(TAG, "Shader (id=$shaderID) created successfully!")
    }

    @Throws
    fun link() {
        glLinkProgram(programID)
        if (glGetProgrami(programID, GL_LINK_STATUS) == 0) {
            throw Exception("Program linking error: ${glGetProgramInfoLog(programID)}")
        }

        Debug.logd(TAG, "Program (id=$programID) linked successfully!")
    }

    fun setUniformMat4f(name: String, mat: Mat4) {
        val loc = glGetUniformLocation(programID, name) // TODO: make uniforms map once and use it
        val arr = memAllocFloat(16)
        glUniformMatrix4fv(loc, false,  mat to arr)
        memFree(arr)
    }

    fun use() {
        glUseProgram(programID)
        Debug.logd(TAG, "Program (id=$programID) activated!")
    }

    fun cleanup() {
        glUseProgram(0)
        glDeleteProgram(programID)
    }
}