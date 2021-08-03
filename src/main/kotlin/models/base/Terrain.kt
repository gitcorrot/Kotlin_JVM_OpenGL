package models.base

import ShaderProgram
import data.Mesh
import data.Triangle
import data.Vertex
import glm_.mat4x4.Mat4
import org.lwjgl.opengl.GL33.*
import utils.Debug
import utils.ResourcesUtils
import utils.TerrainUtils
import kotlin.math.floor

class Terrain(
    val size: Int,
    val a: Float,
    val tileSize: Float
) : Model() {
    companion object {
        val TAG: String = this::class.java.name
        val shaderProgram = ShaderProgram()

        const val VERTEX_SIZE = 6

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

    override var mesh: Mesh? = null

    override fun addMesh(mesh: Mesh) {
        this.mesh = TerrainUtils.generateMesh(size, tileSize, a)
    }

    init {
        this.mesh = TerrainUtils.generateMesh(size, tileSize, a)
        this.create()
    }

    override fun create() {
        if (mesh != null) {
            this.vao = glGenVertexArrays()
            this.vbo = glGenBuffers()
            this.ebo = glGenBuffers()

            uploadVertices(mesh!!, ModelDefault.VERTEX_SIZE)
            uploadIndices(mesh!!)

            // 3 Float vertex coordinates
            glVertexAttribPointer(0, 3, GL_FLOAT, false, VERTEX_SIZE * Float.SIZE_BYTES, 0)
            glEnableVertexAttribArray(0)
            // 3 Float vertex colors
            glVertexAttribPointer(1, 3, GL_FLOAT, false, VERTEX_SIZE * Float.SIZE_BYTES, 3L * Float.SIZE_BYTES)
            glEnableVertexAttribArray(1)

            // Unbind VBO and VAO
            glBindBuffer(GL_ARRAY_BUFFER, 0)
            glBindVertexArray(0)

            Debug.logd(TAG, "models.models.Base.Terrain created!")
        } else {
            throw RuntimeException("Can't create Model without added Mesh!")
        }
    }

    fun draw(viewMat: Mat4, projectionMat: Mat4) {
        shaderProgram.use()
        shaderProgram.setUniformMat4f("m", transformationMat)
        shaderProgram.setUniformMat4f("v", viewMat)
        shaderProgram.setUniformMat4f("p", projectionMat)

        bind()
        glDrawElements(GL_TRIANGLES, getIndicesCount(), GL_UNSIGNED_INT, 0)
    }

    @Throws
    fun getHeightAt(x: Float, z: Float): Float {
        if (this.mesh != null) {
            /*
                     v3         v2
                      |--------/
                      |      /
                      |    /
                      |  /
                      |/
                     v1
            */
            val t = getTriangleAt(x = x, z = z)

            // Calculate plane ax + by + cz + d = 0
            val pA = t.v1.position
            val pB = t.v2.position
            val pC = t.v3.position

            val a: Float = (pB.y - pA.y) * (pC.z - pA.z) - (pC.y - pA.y) * (pB.z - pA.z)
            val b: Float = (pB.z - pA.z) * (pC.x - pA.x) - (pC.z - pA.z) * (pB.x - pA.x)
            val c: Float = (pB.x - pA.x) * (pC.y - pA.y) - (pC.x - pA.x) * (pB.y - pA.y)
            val d: Float = -(a * pA.x + b * pA.y + c * pA.z)

            return (-d - a * x - c * z) / b
        } else {
            println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa")
            throw RuntimeException("Can't get terrain height without added Mesh!")
        }
    }

    private fun getVertexAt(z: Int, x: Int): Vertex {

        if ((z >= size) || (x >= size) || (z < 0) || (x < 0))
            throw RuntimeException("Index out of vertices")

        return mesh!!.vertices[z * (size + 1) + x]
    }

    private fun getTriangleAt(x: Float, z: Float): Triangle {
        // 1. Calculate corners of two triangles:
        /*
               v4             v3
             (z+1,x)       (z+1,x+1)
                  |--------/|
                  |      /  |
                  |    /    |
                  |  /      |
                  |/--------|
             (z,x)         (z,x+1)
              v1             v2
        */
        val xFloor = floor(x / tileSize).toInt()
        val xCeil = xFloor + 1
        val zFloor = floor(-z / tileSize).toInt()
        val zCeil = zFloor + 1

        val v1 = getVertexAt(x = xFloor, z = zFloor)
        val v2 = getVertexAt(x = xCeil, z = zFloor)
        val v3 = getVertexAt(x = xCeil, z = zCeil)
        val v4 = getVertexAt(x = xFloor, z = zCeil)

        // 2. Check if given position is in
        //    triangle (v4, v1, v3) or (v2, v3, v1):
        return if (z < ((v1.position.z - v3.position.z) / (v1.position.x - v3.position.x)) * (x - v1.position.x) + v1.position.z) {
            // We are in(v4, v1, v3)
            Triangle(v4, v1, v3)
        } else {
            // We are in (v2, v3, v1)
            Triangle(v2, v3, v1)
        }
    }

}
