import org.lwjgl.opengl.GL33.*

class ShaderProgram {

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
    }

    @Throws
    fun link() {
        glLinkProgram(programID)
        if (glGetProgrami(programID, GL_LINK_STATUS) == 0) {
            throw Exception("Program linking error: ${glGetProgramInfoLog(programID)}")
        }
    }

    fun use() {
        glUseProgram(programID)
    }

    fun cleanup() {
        glUseProgram(0)
        glDeleteProgram(programID)
    }
}