package models.base

import Texture
import data.Mesh
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.vec3.Vec3
import interfaces.Movable
import interfaces.Rotatable
import interfaces.Scalable
import org.lwjgl.opengl.GL33.*
import org.lwjgl.system.MemoryUtil
import utils.Debug
import java.nio.FloatBuffer
import java.nio.IntBuffer


abstract class Model : Movable, Rotatable, Scalable {
    companion object {
        val TAG: String = this::class.java.name
    }

    override val scale: Vec3 = Vec3(1f)
    override val position: Vec3 = Vec3(0f)
    override var rotation: Quat = Quat()

    var transformationMat = Mat4(1f)

    protected abstract var mesh: Mesh?
    protected abstract fun addMesh(mesh: Mesh)
    protected abstract fun create()

    var vao: Int = -1
        @Throws
        get() {
            if (field == -1)
                throw Exception("Model VAO id not assigned!")
            return field
        }

    var vbo: Int = -1
        @Throws
        get() {
            if (field == -1)
                throw Exception("Model VBO id not assigned!")
            return field
        }

    var ebo: Int = -1
        @Throws
        get() {
            if (field == -1)
                throw Exception("Model EBO id not assigned!")
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

    fun uploadVertices(mesh: Mesh) {
        val verticesBuffer: FloatBuffer =
            MemoryUtil.memAllocFloat(mesh.vertices.size * 5) // each vertex has 5 floats
        for (v in mesh.vertices) {
            verticesBuffer.put(v.convertToFloatArray())
        }
        verticesBuffer.flip() // flip resets position to 0

        glBindVertexArray(vao)
        // for each vertex -> add vertex.getAsArray
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW)
        MemoryUtil.memFree(verticesBuffer)
    }

    fun uploadIndices(mesh: Mesh) {

        val indicesBuffer: IntBuffer = MemoryUtil.memAllocInt(mesh.indices!!.size)
        indicesBuffer
            .put(mesh.indices)
            .flip()

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW)
        MemoryUtil.memFree(indicesBuffer)
    }

    private fun updateTransformationMat() {
        this.transformationMat = Mat4(1f)
            .translate(position)
            .times(rotation.toMat4())
            .scale_(scale)
    }

    fun bind() {
        glBindVertexArray(vao)
    }

    fun cleanup() {
        glDeleteVertexArrays(vao)
    }

    override fun moveBy(v: Vec3) {
        super.moveBy(v)
        updateTransformationMat()
    }

    override fun moveBy(x: Float, y: Float, z: Float) {
        super.moveBy(x, y, z)
        updateTransformationMat()
    }

    override fun moveTo(v: Vec3) {
        super.moveTo(v)
        updateTransformationMat()
    }

    override fun moveTo(x: Float, y: Float, z: Float) {
        super.moveTo(x, y, z)
        updateTransformationMat()
    }

    override fun rotateBy(yaw: Float, pitch: Float, roll: Float) {
        super.rotateBy(yaw, pitch, roll)
        updateTransformationMat()
    }

    override fun rotatePitchBy(angle: Float) {
        super.rotatePitchBy(angle)
        updateTransformationMat()
    }

    override fun rotateRollBy(angle: Float) {
        super.rotateRollBy(angle)
        updateTransformationMat()
    }

    override fun rotateTo(yaw: Float, pitch: Float, roll: Float) {
        super.rotateTo(yaw, pitch, roll)
        updateTransformationMat()
    }

    override fun rotateYawBy(angle: Float) {
        super.rotateYawBy(angle)
        updateTransformationMat()
    }

    override fun scaleBy(x: Float, y: Float, z: Float) {
        super.scaleBy(x, y, z)
        updateTransformationMat()
    }

    override fun scaleTo(scale: Float) {
        super.scaleTo(scale)
        updateTransformationMat()
    }

    override fun scaleTo(x: Float, y: Float, z: Float) {
        super.scaleTo(x, y, z)
        updateTransformationMat()
    }
}
