package models

import TAG
import Texture
import data.Mesh
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import interfaces.Movable
import interfaces.Rotatable
import interfaces.Scalable
import org.lwjgl.opengl.GL33.glBindVertexArray
import org.lwjgl.opengl.GL33.glDeleteVertexArrays
import utils.Debug


abstract class Model() : Movable, Rotatable, Scalable {

    override val scale: Vec3 = Vec3(1f)
    override val translation: Vec3 = Vec3(0f)
    override var rotation: Mat4 = Mat4(1f)

    val transformationMat: Mat4
        get() {
            return Mat4(1f)
                .translate(translation)
                .times(rotation)
                .scale_(scale)
        }

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
