import glm_.mat3x3.Mat3
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL33.*
import org.lwjgl.system.MemoryUtil.memAllocFloat
import org.lwjgl.system.MemoryUtil.memFree
import utils.Debug

class ShaderProgram {
    companion object {
        private val TAG: String = this::class.java.name
    }

    var programID: Int = 0
    private var vertexShaderID: Int = 0
    private var geometryShaderID: Int = 0
    private var fragmentShaderID: Int = 0

    private val uniformsMap = mutableMapOf<String, Int>()

    init {
        programID = glCreateProgram()
    }

    @Throws
    fun createShader(shaderString: String, shaderType: Int) {
        val shaderID = glCreateShader(shaderType)
        glShaderSource(shaderID, shaderString)
        glCompileShader(shaderID)

        if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == 0) {
            throw Exception("Vertex Shader Info Log: ${glGetShaderInfoLog(shaderID)}")
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

    private fun getUniformLocation(name: String): Int {
        if (uniformsMap[name] == null) {
            uniformsMap[name] = glGetUniformLocation(programID, name)
        }
        return uniformsMap[name]!!
    }

    fun setUniformMat4f(name: String, mat: Mat4) {
        val uniformLocation = getUniformLocation(name)
        val arr = memAllocFloat(16)
        glUniformMatrix4fv(uniformLocation, false, mat to arr)
        memFree(arr)
    }

    fun setUniformMat3f(name: String, mat: Mat3) {
        val uniformLocation = getUniformLocation(name)
        val arr = memAllocFloat(9)
        glUniformMatrix3fv(uniformLocation, false, mat to arr)
        memFree(arr)
    }

    fun setUniformVec3f(name: String, vec: Vec3) {
        val uniformLocation = getUniformLocation(name)
        glUniform3f(uniformLocation, vec.x, vec.y, vec.z)
    }

    fun setUniformFloat(name: String, value: Float) {
        val uniformLocation = getUniformLocation(name)
        glUniform1f(uniformLocation, value)
    }

    fun setUniformInt(name: String, value: Int) {
        val uniformLocation = getUniformLocation(name)
        glUniform1i(uniformLocation, value)
    }

    fun use() {
        glUseProgram(programID)
//        Debug.logd(TAG, "Program (id=$programID) activated!")
    }

    fun cleanup() {
        glUseProgram(0)
        glDeleteProgram(programID)
    }
}