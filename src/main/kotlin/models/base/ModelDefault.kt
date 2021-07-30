package models.base

import ShaderProgram
import collision.AxisAlignedBoundingBox
import collision.OrientedBoundingBox
import data.Mesh
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL33.*
import utils.Debug
import utils.ResourcesUtils

abstract class ModelDefault : Model() {
    companion object {
        val TAG: String = this::class.java.name
        val shaderProgram = ShaderProgram()

        const val VERTEX_SIZE = 8

        private const val vertexShaderPath = "model_default_vertex_shader.glsl"
        private const val fragmentShaderPath = "model_default_fragment_shader.glsl"

        init {
            val vertexShaderString = ResourcesUtils.readShader(vertexShaderPath)
            val fragmentShaderString = ResourcesUtils.readShader(fragmentShaderPath)
            shaderProgram.createShader(vertexShaderString, GL_VERTEX_SHADER)
            shaderProgram.createShader(fragmentShaderString, GL_FRAGMENT_SHADER)
            shaderProgram.link()
            shaderProgram.use()

            // Shader's sampler2d belong to texture unit 2
            shaderProgram.setUniformInt("colorPaletteTexture", 2)
        }
    }

    lateinit var axisAlignedBoundingBox: AxisAlignedBoundingBox
    lateinit var orientedBoundingBox: OrientedBoundingBox
    override var mesh: Mesh? = null

    override fun addMesh(mesh: Mesh) {
        this.mesh = mesh

        axisAlignedBoundingBox = AxisAlignedBoundingBox(mesh)
        orientedBoundingBox = OrientedBoundingBox(mesh)
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
            // 3 Float vertex normals
            glVertexAttribPointer(1, 3, GL_FLOAT, false, VERTEX_SIZE * Float.SIZE_BYTES, 3L * Float.SIZE_BYTES)
            glEnableVertexAttribArray(1)
            // 2 Float vertex texture coordinates
            glVertexAttribPointer(2, 2, GL_FLOAT, false, VERTEX_SIZE * Float.SIZE_BYTES, 6L * Float.SIZE_BYTES)
            glEnableVertexAttribArray(2)

            // Unbind VBO and VAO
            glBindBuffer(GL_ARRAY_BUFFER, 0)
            glBindVertexArray(0)

            Debug.logd(TAG, "models.Base.ModelDefault created!")
        } else {
            throw RuntimeException("Can't create Model without added Mesh!")
        }
    }

    fun draw(viewMat: Mat4, projectionMat: Mat4) {
        shaderProgram.use()
        shaderProgram.setUniformMat4f("m", transformationMat)
        shaderProgram.setUniformMat4f("v", viewMat)
        shaderProgram.setUniformMat4f("p", projectionMat)

        val modelNormalMat = glm.transpose(glm.inverse(transformationMat.toMat3()))
        shaderProgram.setUniformMat3f("normalMatrix", modelNormalMat)

        bind()
        texture.bind()

        glDrawElements(GL_TRIANGLES, getIndicesCount(), GL_UNSIGNED_INT, 0)
    }

    fun drawBoundingBoxes() {
        ModelNoLight.shaderProgram.use()
        axisAlignedBoundingBox.draw()
        orientedBoundingBox.draw()
    }

    // Apply transformations to bounding box
    override fun scaleBy(x: Float, y: Float, z: Float) {
        super.scaleBy(x, y, z)
        axisAlignedBoundingBox.update(transformationMat)
        orientedBoundingBox.update(transformationMat)
    }

    override fun scaleTo(scale: Float) {
        super.scaleTo(scale)
        axisAlignedBoundingBox.update(transformationMat)
        orientedBoundingBox.update(transformationMat)
    }

    override fun scaleTo(x: Float, y: Float, z: Float) {
        super.scaleTo(x, y, z)
        axisAlignedBoundingBox.update(transformationMat)
        orientedBoundingBox.update(transformationMat)
    }

    override fun moveBy(v: Vec3) {
        super.moveBy(v)
        axisAlignedBoundingBox.update(transformationMat)
        orientedBoundingBox.update(transformationMat)
    }

    override fun moveBy(x: Float, y: Float, z: Float) {
        super.moveBy(x, y, z)
        axisAlignedBoundingBox.update(transformationMat)
        orientedBoundingBox.update(transformationMat)
    }

    override fun moveTo(v: Vec3) {
        super.moveTo(v)
        axisAlignedBoundingBox.update(transformationMat)
        orientedBoundingBox.update(transformationMat)
    }

    override fun moveTo(x: Float, y: Float, z: Float) {
        super.moveTo(x, y, z)
        axisAlignedBoundingBox.update(transformationMat)
        orientedBoundingBox.update(transformationMat)
    }

    override fun rotatePitchBy(angle: Float) {
        super.rotatePitchBy(angle)
        axisAlignedBoundingBox.update(transformationMat)
        orientedBoundingBox.update(transformationMat)
    }

    override fun rotateYawBy(angle: Float) {
        super.rotateYawBy(angle)
        axisAlignedBoundingBox.update(transformationMat)
        orientedBoundingBox.update(transformationMat)
    }

    override fun rotateRollBy(angle: Float) {
        super.rotateRollBy(angle)
        axisAlignedBoundingBox.update(transformationMat)
        orientedBoundingBox.update(transformationMat)
    }

    override fun rotateBy(angle: Float, axis: Vec3) {
        super.rotateBy(angle, axis)
        axisAlignedBoundingBox.update(transformationMat)
        orientedBoundingBox.update(transformationMat)
    }

    override fun rotateBy(quat: Quat) {
        super.rotateBy(quat)
        axisAlignedBoundingBox.update(transformationMat)
        orientedBoundingBox.update(transformationMat)
    }

    override fun rotateTo(yaw: Float, pitch: Float, roll: Float) {
        super.rotateTo(yaw, pitch, roll)
        axisAlignedBoundingBox.update(transformationMat)
        orientedBoundingBox.update(transformationMat)
    }
}
