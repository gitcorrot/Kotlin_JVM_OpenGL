package models

import TAG
import Texture
import data.Mesh
import data.Transformation
import interfaces.Movable
import org.lwjgl.opengl.GL33.glBindVertexArray
import org.lwjgl.opengl.GL33.glDeleteVertexArrays
import utils.Debug


abstract class Model: Movable {

    override val transformation = Transformation()

    abstract var mesh: Mesh
    abstract fun create()

    var vao: Int = -1
        @Throws
        get() {
            if (field == -1)
                throw Exception("models.Model VAO id not assigned!")
            return field
        }

    var texture = Texture()


    fun getIndicesCount() = this.mesh.indices?.size ?: 0

    fun addTexture(path: String) {
        bind()
        texture.createTexture(path)
        Debug.logd(TAG, "Texture added to model!")
    }

    fun addTexture(texture: Texture) {
        this.texture = texture
        Debug.logd(TAG, "Texture added to model!")
    }

    fun bind() {
        glBindVertexArray(vao)
    }

    fun cleanup() {
        glDeleteVertexArrays(vao)
    }
}
