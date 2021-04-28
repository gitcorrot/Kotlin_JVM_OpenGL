import data.Mesh
import data.Transformation
import data.Vertex
import glm_.glm
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import models.Model
import org.lwjgl.opengl.GL33.*
import org.lwjgl.system.MemoryUtil
import utils.Debug
import java.nio.FloatBuffer

class Terrain() : Model() {
    companion object {
        val TAG: String = this::class.java.name
//        val shaderProgram = ShaderProgram()
//
//        private const val vertexShaderPath = "model_default_vertex_shader.glsl"
//        private const val fragmentShaderPath = "model_default_fragment_shader.glsl"
//
//        init {
//            val vertexShaderString = ResourcesUtils.readShader(vertexShaderPath)
//            val fragmentShaderString = ResourcesUtils.readShader(fragmentShaderPath)
//            shaderProgram.createShader(vertexShaderString, GL_VERTEX_SHADER)
//            shaderProgram.createShader(fragmentShaderString, GL_FRAGMENT_SHADER)
//            shaderProgram.link()
//            shaderProgram.use()
//        }
    }

    override var mesh: Mesh = Mesh(ArrayList())
    override val transformation = Transformation()

    override fun create() {
        val verticesBuffer: FloatBuffer = MemoryUtil.memAllocFloat(mesh.vertices.size * 8) // each vertex has 8 floats
        for (v in mesh.vertices) {
            verticesBuffer.put(v.convertToFloatArray())
        }
        verticesBuffer.flip() // flip resets position to 0

        this.vao = glGenVertexArrays()
        val vbo = glGenBuffers()

        glBindVertexArray(vao)
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW)
        MemoryUtil.memFree(verticesBuffer)

        // 3 Float vertex coordinates
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * 4, 0)
        glEnableVertexAttribArray(0)
        // 3 Float vertex normals
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * 4, 3 * 4)
        glEnableVertexAttribArray(1)
        // 2 Float vertex texture coordinates
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * 4, 6 * 4)
        glEnableVertexAttribArray(2)

        // Unbind VBO and VAO
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)

        Debug.logd(TAG, "models.Terrain created!")
    }

    fun generateMesh(size: Int) {

        //    (i,j)      (i,j+1)
        //     lt         rt
        //      |------- -|
        //      |      -  |
        //      |    -    |
        //      |  -      |
        //      |- ------ |
        //     lb        rb
        //   (i+1,j)    (i+1,j+1)

        for (i in 0 until size) {
            for (j in 0 until size) {

                val lt = Vertex()
                val rt = Vertex()
                val lb = Vertex()
                val rb = Vertex()

                val a = 0.075f
                val tileSize = 25f

                lt.position.x = j * tileSize
                rt.position.x = (j + 1) * tileSize
                lb.position.x = j * tileSize
                rb.position.x = (j + 1) * tileSize

                lt.position.y = glm.simplex(Vec2(i * a, j * a)) * tileSize
                rt.position.y = glm.simplex(Vec2(i * a, (j + 1) * a)) * tileSize
                lb.position.y = glm.simplex(Vec2((i + 1) * a, j * a)) * tileSize
                rb.position.y = glm.simplex(Vec2((i + 1) * a, (j + 1) * a)) * tileSize

                lt.position.z = i * tileSize
                rt.position.z = i * tileSize
                lb.position.z = (i + 1) * tileSize
                rb.position.z = (i + 1) * tileSize

                lt.textureCoordinates.x = j.toFloat()
                rt.textureCoordinates.x = (j + 1).toFloat()
                lb.textureCoordinates.x = j.toFloat()
                rb.textureCoordinates.x = (j + 1).toFloat()

                lt.textureCoordinates.y = i.toFloat()
                rt.textureCoordinates.y = i.toFloat()
                lb.textureCoordinates.y = (i + 1).toFloat()
                rb.textureCoordinates.y = (i + 1).toFloat()

                // Normal 1 -> lb, rt, lt
                val n1 = calculateNormal(lb.position, rt.position, lt.position)
                // Normal 2 -> lb, rb, rt
                val n2 = calculateNormal(lb.position, rb.position, rt.position)

                // Triangle 1
                this.mesh.vertices.add(Vertex(lb.position, n1, lb.textureCoordinates))
                this.mesh.vertices.add(Vertex(rt.position, n1, rt.textureCoordinates))
                this.mesh.vertices.add(Vertex(lt.position, n1, lt.textureCoordinates))
                // Triangle 2
                this.mesh.vertices.add(Vertex(lb.position, n2, lb.textureCoordinates))
                this.mesh.vertices.add(Vertex(rb.position, n2, rb.textureCoordinates))
                this.mesh.vertices.add(Vertex(rt.position, n2, rt.textureCoordinates))
            }
        }
    }

    private fun calculateNormal(v1: Vec3, v2: Vec3, v3: Vec3): Vec3 {
        val nv1 = v2.minus(v1)
        val nv2 = v3.minus(v1)

        return glm.normalize(nv1.cross(nv2))
    }
}