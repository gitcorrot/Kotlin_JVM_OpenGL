//package collision
//
//import Texture
//import data.Mesh
//import models.base.Model
//import org.lwjgl.opengl.GL33.*
//import utils.Debug
//
//class BoundingBox(
//    val primaryMesh: Mesh,
//    var boundingPoints: BoundingPoints
//) : Model(
//    primaryMesh,
//    Texture.getDefaultColorPalette()
//) {
//    companion object {
//        const val VERTEX_SIZE = 5
//    }
//
//    init {
//        this.vao = glGenVertexArrays()
//        this.vbo = glGenBuffers()
//        this.ebo = glGenBuffers()
//
//        uploadVertices(primaryMesh, vertexSize = VERTEX_SIZE)
//        uploadIndices(primaryMesh)
//
//        // 3 Float vertex coordinates
//        glVertexAttribPointer(0, 3, GL_FLOAT, false, VERTEX_SIZE * Float.SIZE_BYTES, 0)
//        glEnableVertexAttribArray(0)
//        // 2 Float vertex texture coordinates
//        glVertexAttribPointer(1, 2, GL_FLOAT, false, VERTEX_SIZE * Float.SIZE_BYTES, 3L * Float.SIZE_BYTES)
//        glEnableVertexAttribArray(1)
//
//        // Unbind VBO and VAO
//        glBindBuffer(GL_ARRAY_BUFFER, 0)
//        glBindVertexArray(0)
//
//        Debug.logd(TAG, "AxisAlignedBoundingBox created!")
//    }
//}