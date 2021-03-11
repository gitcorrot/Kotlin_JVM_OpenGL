import org.lwjgl.Version.getVersion
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL33.*
import org.lwjgl.system.MemoryUtil.NULL
import org.lwjgl.system.MemoryUtil.memAllocFloat
import java.nio.FloatBuffer

// https://github.com/Zi0P4tch0/LWJGL-Kotlin-Example/blob/master/src/main/kotlin/com/example/Engine.kt

const val FPS = 60
private var window: Long? = null

private var errCallback: GLFWErrorCallback? = null
private var keyCallback: GLFWKeyCallback? = null

fun init() {

    // Setup an error callback. The default implementation
    // will print the error message in System.err.
    errCallback = glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err))

    if (!glfwInit()) {
        throw IllegalStateException("Unable to initialize GLFW")
    }

    // Configure our window
    glfwDefaultWindowHints()
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)

    // Create the window
    window = glfwCreateWindow(800, 600, "Hello World!", NULL, NULL)
    if (window == NULL) {
        throw RuntimeException("Failed to create the GLFW window")
    }

    // Get the resolution of the primary monitor
    glfwGetVideoMode(glfwGetPrimaryMonitor())?.let {
        // Center our window
        glfwSetWindowPos(
            window!!,
            (it.width() - 800) / 2,
            (it.height() - 600) / 2
        )
    }

    // Setup a key callback. It will be called every time a key is pressed, repeated or released.
    keyCallback = glfwSetKeyCallback(window!!, object : GLFWKeyCallback() {
        override fun invoke(
            window: Long,
            key: Int,
            scancode: Int,
            action: Int,
            mods: Int
        ) {
            if (key == GLFW_KEY_Q && action == GLFW_PRESS) {
                glfwSetWindowShouldClose(window, true)
            }
            if (key == GLFW_KEY_SPACE && action == GLFW_RELEASE) {
                print('*')
            }
        }
    })

    // Make the OpenGL context current
    glfwMakeContextCurrent(window!!)

    // Enable v-sync
    glfwSwapInterval(1)

    // Make the window visible
    glfwShowWindow(window!!)

    // This line is critical for LWJGL's interoperation with GLFW's
    // OpenGL context, or any context that is managed externally.
    // LWJGL detects the context that is current in the current thread,
    // creates the GLCapabilities instance and makes the OpenGL
    // bindings available for use.
    GL.createCapabilities()

    println("JLWGL Version: ${getVersion()}")
    println("OpenGL Version: ${glGetString(GL_VERSION)}")
}

fun loop() {

    var lastLoopTime = glfwGetTime()

    // Load vertex shader
    val vertexShaderString = {}.javaClass.getResource("Shaders/vertex_shader.glsl").readText()
    val vertexShader = glCreateShader(GL_VERTEX_SHADER)
    glShaderSource(vertexShader, vertexShaderString)
    glCompileShader(vertexShader)
    if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) != 1) {
        println("Vertex Shader Info Log: ${glGetShaderInfoLog(vertexShader)}")
    }

    // Load fragment shader
    val fragmentShaderString = {}.javaClass.getResource("Shaders/fragment_shader.glsl").readText()
    val fragmentShader = glCreateShader(GL_FRAGMENT_SHADER)
    glShaderSource(fragmentShader, fragmentShaderString)
    glCompileShader(fragmentShader)
    if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) != 1) {
        println("Fragment Shader Info Log: ${glGetShaderInfoLog(fragmentShader)}")
    }

    // Create program
    val program = glCreateProgram()
    glAttachShader(program, vertexShader)
    glAttachShader(program, fragmentShader)
    glLinkProgram(program)
    if (glGetProgrami(program, GL_LINK_STATUS) != 1) {
        println("Program Info Log: ${glGetProgramInfoLog(fragmentShader)}")
    }

    // primitive type array
    val vertices = floatArrayOf(
        -0.5f, -0.5f, 0.0f,
        0.5f, -0.5f, 0.0f,
        0.0f, 0.5f, 0.0f,
    )

    // Create floats buffer and fill with vertices
    val verticesBuffer: FloatBuffer = memAllocFloat(vertices.size)
    verticesBuffer
        .put(vertices)
        .flip() // flip resets position to 0

    // VAO and VBO
    val vao = glGenVertexArrays()
    val vbo = glGenBuffers()

    glBindVertexArray(vao)

    glBindBuffer(GL_ARRAY_BUFFER, vbo)
    glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW)

    glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)

    while (!glfwWindowShouldClose(window!!)) {

        // Set the clear color
        glClearColor(.5f, .6f, .7f, 0.0f)
        // Clear the frame buffer
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // Render triangle
        glUseProgram(program)
        glBindVertexArray(vao)
        glEnableVertexAttribArray(0)
        glDrawArrays(GL_TRIANGLES, 0, 3)

        // Swap the color buffers
        glfwSwapBuffers(window!!)

        // Poll for window events. The key callback above will only be
        // invoked during this call.
        glfwPollEvents()

//        Not neccesary until we have v -sync and call glfwSwapBuffers
//        Thread.sleep(((lastLoopTime + 1000 / FPS) - glfwGetTime()).toLong())
//        lastLoopTime = glfwGetTime()
    }

    // Cleanup
    glDeleteVertexArrays(vao)
    glDeleteBuffers(vbo)
    glDeleteProgram(program)
    glDeleteShader(vertexShader);
    glDeleteShader(fragmentShader);
}

fun main() {
    init()
    loop()

    // Destroy window
    println("\nDestroying window...")
    glfwDestroyWindow(window!!)
    keyCallback?.free()

    // Terminate GLFW
    glfwTerminate()
    glfwSetErrorCallback(null)?.free()
    println("Window destroyed!")
}
