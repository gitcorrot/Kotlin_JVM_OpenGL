package collision

import Texture
import data.Mesh
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import models.base.Model
import org.lwjgl.opengl.GL33.*
import utils.Debug

class OrientedBoundingBox(modelMesh: Mesh) : Model(modelMesh, Texture.getDefaultColorPalette()) {

    companion object {
        val TAG: String = this::class.java.name
        val uvs = Vec2(0.125f, 0.0f) // Orange

        const val VERTEX_SIZE = 5
    }

    var boundingPoints: BoundingPoints
    var primaryMesh: Mesh

    init {
        boundingPoints = BoundingBoxUtils.calculateBoundingPoints(modelMesh.vertices)
        primaryMesh = BoundingBoxUtils.createMesh(boundingPoints, uvs)

        this.vao = glGenVertexArrays()
        this.vbo = glGenBuffers()
        this.ebo = glGenBuffers()

        uploadVertices(primaryMesh, vertexSize = VERTEX_SIZE)
        uploadIndices(primaryMesh)

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
    }

    fun update(transformationMat: Mat4) {
//        // Calculate oriented bounding box
//        val transformedVertices = BoundingBoxUtils.calculateTransformedMeshVertices(primaryMesh, transformationMat)
//
//        // Calculate axis aligned bounding box over oriented one
//        boundingPoints = BoundingBoxUtils.calculateBoundingPoints(transformedVertices)
//        val boundingMesh = BoundingBoxUtils.createMesh(boundingPoints, uvs)
//
//        uploadVertices(boundingMesh, vertexSize = VERTEX_SIZE)
    }
}