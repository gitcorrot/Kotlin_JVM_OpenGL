package collision

import Texture
import data.Mesh
import glm_.vec2.Vec2
import models.base.Model
import org.lwjgl.opengl.GL33.*
import utils.Debug

class OrientedBoundingBox(modelMesh: Mesh) : Model() {
    companion object {
        val TAG: String = this::class.java.name
        val uvs = Vec2(0.125f, 0.0f) // Orange
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

            uploadVertices(mesh!!)
            uploadIndices(mesh!!)

            // 3 Float vertex coordinates
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * 4, 0)
            glEnableVertexAttribArray(0)
            // 2 Float vertex texture coordinates
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * 4, 3 * 4)
            glEnableVertexAttribArray(1)

            // Unbind VBO and VAO
            glBindBuffer(GL_ARRAY_BUFFER, 0)
            glBindVertexArray(0)

            Debug.logd(TAG, "OrientedBoundingBox created!")
        } else {
            throw RuntimeException("Can't create Model without added Mesh!")
        }
    }
}