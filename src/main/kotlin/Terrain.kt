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
import utils.ResourcesUtils
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Terrain(
    private val a: Float,
    val tileSize: Float
) : Model() {
    companion object {
        val TAG: String = this::class.java.name
        val shaderProgram = ShaderProgram()

        private const val vertexShaderPath = "terrain_vertex_shader.glsl"
        private const val geometryShaderPath = "terrain_geometry_shader.glsl"
        private const val fragmentShaderPath = "terrain_fragment_shader.glsl"

        init {
            val vertexShaderString = ResourcesUtils.readShader(vertexShaderPath)
            val geometryShaderString = ResourcesUtils.readShader(geometryShaderPath)
            val fragmentShaderString = ResourcesUtils.readShader(fragmentShaderPath)
            shaderProgram.createShader(vertexShaderString, GL_VERTEX_SHADER)
            shaderProgram.createShader(geometryShaderString, GL_GEOMETRY_SHADER)
            shaderProgram.createShader(fragmentShaderString, GL_FRAGMENT_SHADER)
            shaderProgram.link()
            shaderProgram.use()
        }
    }

    var size: Int = 0

    override var mesh: Mesh = Mesh(ArrayList())
    override val transformation = Transformation()

    override fun create() {
        val verticesBuffer: FloatBuffer = MemoryUtil.memAllocFloat(mesh.vertices.size * 9) // each vertex has 9 floats
        for (v in mesh.vertices) {
            verticesBuffer.put(v.convertToFloatArray())
        }
        verticesBuffer.flip() // flip resets position to 0

        val indicesBuffer: IntBuffer = MemoryUtil.memAllocInt(mesh.indices!!.size)
        indicesBuffer
            .put(mesh.indices)
            .flip()

        this.vao = glGenVertexArrays()
        val vbo = glGenBuffers()
        val ebo = glGenBuffers()

        glBindVertexArray(vao)

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW)
        MemoryUtil.memFree(verticesBuffer)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW)
        MemoryUtil.memFree(indicesBuffer)

        // 3 Float vertex coordinates
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * 4, 0)
        glEnableVertexAttribArray(0)
        // 3 Float vertex colors
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * 4, 3 * 4)
        glEnableVertexAttribArray(1)

        // Unbind VBO and VAO
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)

        Debug.logd(TAG, "models.Terrain created!")
    }

    fun getHeightAt(x: Int, z: Int): Float {
        return this.mesh.vertices[(z * (size + 1)) + x].position.y
    }

    fun generateMesh(size: Int) {
        this.size = size

//        // Generate heightmap vertices
//        for (x in size downTo 0) {
//            for (z in size downTo 0) {

        for (z in 0..size) {
            for (x in 0..size) {
                val position = Vec3()
                position.x = x * tileSize
                position.y = glm.simplex(Vec2(z * a, x * a)) * tileSize
                position.z = -z * tileSize

                val color = Vec3(0.4f, 0.6f, 0.3f)

                this.mesh.vertices.add(
                    Vertex(
                        position = position,
                        normal = null,
                        color = color,
                        textureCoordinates = null
                    )
                )
            }
        }

        // Calculate indices
        val indices = arrayListOf<Int>()

        //   (z+1,x)     (z+1,x+1)
        //      |------- -|
        //      |      -  |
        //      |    -    |
        //      |  -      |
        //      |- ------ |
        //   (z,x)      (z,x+1)
        for (z in 0 until size) {
            for (x in 0 until size) {
                val lb = z * (size + 1) + x
                val rb = z * (size + 1) + x + 1
                val lt = (z + 1) * (size + 1) + x
                val rt = (z + 1) * (size + 1) + x + 1

                // triangle 1 -> lb, rt, lt
                indices.add(lt)
                indices.add(lb)
                indices.add(rt)

                // triangle 2 -> lb, rb, rt
                indices.add(rb)
                indices.add(rt)
                indices.add(lb)
//                indices.add(rt)
//                indices.add(lb)
//                indices.add(rb)
            }
        }
        this.mesh.indices = indices.toIntArray()


//        val x = 0
//        val z = 10
//        Debug.logd(TAG, this.mesh.vertices[(z * (size + 1)) + x].toString())
//        this.mesh.vertices[(z * (size + 1)) + x].color!!.plusAssign(0f, 1f, 0f)
    }

//    fun generateMesh(size: Int) {
//        this.size = size
//
//        //    (i,j)      (i,j+1)
//        //     lt         rt
//        //      |------- -|
//        //      |      -  |
//        //      |    -    |
//        //      |  -      |
//        //      |- ------ |
//        //     lb        rb
//        //   (i+1,j)    (i+1,j+1)
//
//        for (i in 0 until size) {
//            for (j in 0 until size) {
//
//                val lt = Vertex(position = Vec3(), normal = Vec3(), color = Vec3(), null)
//                val rt = Vertex(position = Vec3(), normal = Vec3(), color = Vec3(), null)
//                val lb = Vertex(position = Vec3(), normal = Vec3(), color = Vec3(), null)
//                val rb = Vertex(position = Vec3(), normal = Vec3(), color = Vec3(), null)
//
//                lt.position.x = j * tileSize
//                rt.position.x = (j + 1) * tileSize
//                lb.position.x = j * tileSize
//                rb.position.x = (j + 1) * tileSize
//
//                lt.position.y = glm.simplex(Vec2(i * a, j * a)) * tileSize
//                rt.position.y = glm.simplex(Vec2(i * a, (j + 1) * a)) * tileSize
//                lb.position.y = glm.simplex(Vec2((i + 1) * a, j * a)) * tileSize
//                rb.position.y = glm.simplex(Vec2((i + 1) * a, (j + 1) * a)) * tileSize
//
//                lt.position.z = i * tileSize
//                rt.position.z = i * tileSize
//                lb.position.z = (i + 1) * tileSize
//                rb.position.z = (i + 1) * tileSize
//
//                lt.color = Vec3(0f, 0f, i / size.toFloat())
//                rt.color = Vec3(0f, 0f, i / size.toFloat())
//                lb.color = Vec3(0f, 0f, i / size.toFloat())
//                rb.color = Vec3(0f, 0f, i / size.toFloat())
//
////                lt.textureCoordinates.x = j.toFloat()
////                rt.textureCoordinates.x = (j + 1).toFloat()
////                lb.textureCoordinates.x = j.toFloat()
////                rb.textureCoordinates.x = (j + 1).toFloat()
////
////                lt.textureCoordinates.y = i.toFloat()
////                rt.textureCoordinates.y = i.toFloat()
////                lb.textureCoordinates.y = (i + 1).toFloat()
////                rb.textureCoordinates.y = (i + 1).toFloat()
//
//                // Normal 1 -> lb, rt, lt
//                val n1 = calculateNormal(lb.position, rt.position, lt.position)
//                // Normal 2 -> lb, rb, rt
//                val n2 = calculateNormal(lb.position, rb.position, rt.position)
//
//                // Triangle 1
//                this.mesh.vertices.add(Vertex(lb.position, n1, lb.color, null))
//                this.mesh.vertices.add(Vertex(rt.position, n1, rt.color, null))
//                this.mesh.vertices.add(Vertex(lt.position, n1, lt.color, null))
//                // Triangle 2, null
//                this.mesh.vertices.add(Vertex(lb.position, n2, lb.color, null))
//                this.mesh.vertices.add(Vertex(rb.position, n2, rb.color, null))
//                this.mesh.vertices.add(Vertex(rt.position, n2, rt.color, null))
//            }
//        }
//    }

    private fun calculateNormal(v1: Vec3, v2: Vec3, v3: Vec3): Vec3 {
        val nv1 = v2.minus(v1)
        val nv2 = v3.minus(v1)

        return glm.normalize(nv1.cross(nv2))
    }
}
