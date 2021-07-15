package models.base

import TAG
import Texture
import data.Mesh
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.vec3.Vec3
import interfaces.Movable
import interfaces.Rotatable
import interfaces.Scalable
import org.lwjgl.opengl.GL33.glBindVertexArray
import org.lwjgl.opengl.GL33.glDeleteVertexArrays
import utils.Debug


abstract class Model() : Movable, Rotatable, Scalable {

    override val scale: Vec3 = Vec3(1f)
    override val position: Vec3 = Vec3(0f)
    override var rotation: Quat = Quat()

    val transformationMat: Mat4
        get() {
            return Mat4(1f)
                .translate(position)
                .times(rotation.toMat4())
                .scale_(scale)
        }

    protected abstract var mesh: Mesh?
    protected abstract fun addMesh(mesh: Mesh)
    protected abstract fun create()

    var vao: Int = -1
        @Throws
        get() {
            if (field == -1)
                throw Exception("models.Base.Model VAO id not assigned!")
            return field
        }

    var texture = Texture()


    fun getIndicesCount() = this.mesh?.indices?.size ?: 0

    fun addTexture(path: String) {
        this.bind()
        this.texture.createTexture(path)
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
