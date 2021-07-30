package collision

import Texture
import data.Mesh
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import models.base.Model
import models.base.ModelNoLight
import org.lwjgl.opengl.GL33.*
import utils.Debug

class OrientedBoundingBox(modelMesh: Mesh) : Model() {
    companion object {
        val TAG: String = this::class.java.name
        val uvs = Vec2(0.125f, 0.0f) // Orange

        const val VERTEX_SIZE = 5
    }

    private var boundingPoints = BoundingPoints()
    override var mesh: Mesh? = null

    init {
        addTexture(Texture.getDefaultColorPalette())
        boundingPoints = BoundingBoxUtils.calculateBoundingPoints(modelMesh.vertices)
        mesh = BoundingBoxUtils.createMesh(boundingPoints, uvs)
        create()
    }

    override fun addMesh(mesh: Mesh) {
        this.mesh = mesh
    }

    override fun create() {
        if (mesh != null) {
            this.vao = glGenVertexArrays()
            this.vbo = glGenBuffers()
            this.ebo = glGenBuffers()

            uploadVertices(mesh!!, VERTEX_SIZE)
            uploadIndices(mesh!!)

            // 3 Float vertex coordinates
            glVertexAttribPointer(0, 3, GL_FLOAT, false, VERTEX_SIZE * Float.SIZE_BYTES, 0)
            glEnableVertexAttribArray(0)
            // 2 Float vertex texture coordinates
            glVertexAttribPointer(1, 2, GL_FLOAT, false, VERTEX_SIZE * Float.SIZE_BYTES, 3L * Float.SIZE_BYTES)
            glEnableVertexAttribArray(1)

            // Unbind VBO and VAO
            glBindBuffer(GL_ARRAY_BUFFER, 0)
            glBindVertexArray(0)

            Debug.logd(TAG, "OrientedBoundingBox created!")
        } else {
            throw RuntimeException("Can't create Model without added Mesh!")
        }
    }

    fun update(transformationMat: Mat4) {
        this.transformationMat = transformationMat
    }

    fun draw() {
        ModelNoLight.shaderProgram.setUniformMat4f("m", transformationMat)
        bind()
        texture.bind()
        glDrawElements(GL_LINES, getIndicesCount(), GL_UNSIGNED_INT, 0)
    }
}