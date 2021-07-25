package models.base

import ShaderProgram
import collision.AxisAlignedBoundingBox
import collision.OrientedBoundingBox
import data.Mesh
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL33.*
import org.lwjgl.system.MemoryUtil
import utils.Debug
import utils.ResourcesUtils
import java.nio.FloatBuffer
import java.nio.IntBuffer

abstract class ModelNoLight : Model() {
    companion object {
        val TAG: String = this::class.java.name
        val shaderProgram = ShaderProgram()

        private const val vertexShaderPath = "model_no_light_vertex_shader.glsl"
        private const val fragmentShaderPath = "model_no_light_fragment_shader.glsl"

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
        // Create bounding boxes
        axisAlignedBoundingBox = AxisAlignedBoundingBox(mesh)
        orientedBoundingBox = OrientedBoundingBox(mesh)
    }

    override fun create() {
        if (mesh != null) {
            this.vao = glGenVertexArrays()
            this.vbo = glGenBuffers()
            this.ebo = glGenBuffers()

            val verticesBuffer: FloatBuffer =
                MemoryUtil.memAllocFloat(mesh!!.vertices.size * 5) // each vertex has 5 floats
            for (v in mesh!!.vertices) {
                verticesBuffer.put(v.convertToFloatArray())
            }
            verticesBuffer.flip() // flip resets position to 0

            val indicesBuffer: IntBuffer = MemoryUtil.memAllocInt(mesh!!.indices!!.size)
            indicesBuffer
                .put(mesh!!.indices)
                .flip()

            glBindVertexArray(vao)
            // for each vertex -> add vertex.getAsArray
            glBindBuffer(GL_ARRAY_BUFFER, vbo)
            glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW)
            MemoryUtil.memFree(verticesBuffer)

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW)
            MemoryUtil.memFree(indicesBuffer)

            // 3 Float vertex coordinates
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * 4, 0)
            glEnableVertexAttribArray(0)
            // 2 Float vertex texture coordinates
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * 4, 3 * 4)
            glEnableVertexAttribArray(1)

            // Unbind VBO and VAO
            glBindBuffer(GL_ARRAY_BUFFER, 0)
            glBindVertexArray(0)

            Debug.logd(TAG, "ModelNoLight created!")
        } else {
            throw RuntimeException("Can't create Model without added Mesh!")
        }
    }

    // Apply transformations to bounding box
    override fun scaleBy(x: Float, y: Float, z: Float) {
        super.scaleBy(x, y, z)
        axisAlignedBoundingBox.update(transformationMat)
    }

    override fun scaleTo(scale: Float) {
        super.scaleTo(scale)
        axisAlignedBoundingBox.update(transformationMat)
    }

    override fun scaleTo(x: Float, y: Float, z: Float) {
        super.scaleTo(x, y, z)
        axisAlignedBoundingBox.update(transformationMat)
    }

    override fun moveBy(v: Vec3) {
        super.moveBy(v)
        axisAlignedBoundingBox.update(transformationMat)
    }

    override fun moveBy(x: Float, y: Float, z: Float) {
        super.moveBy(x, y, z)
        axisAlignedBoundingBox.update(transformationMat)
    }

    override fun moveTo(v: Vec3) {
        super.moveTo(v)
        axisAlignedBoundingBox.update(transformationMat)
    }

    override fun moveTo(x: Float, y: Float, z: Float) {
        super.moveTo(x, y, z)
        axisAlignedBoundingBox.update(transformationMat)
    }

    override fun rotateBy(yaw: Float, pitch: Float, roll: Float) {
        super.rotateBy(yaw, pitch, roll)
        axisAlignedBoundingBox.update(transformationMat)
    }

    override fun rotatePitchBy(angle: Float) {
        super.rotatePitchBy(angle)
        axisAlignedBoundingBox.update(transformationMat)
    }

    override fun rotateRollBy(angle: Float) {
        super.rotateRollBy(angle)
        axisAlignedBoundingBox.update(transformationMat)
    }

    override fun rotateTo(yaw: Float, pitch: Float, roll: Float) {
        super.rotateTo(yaw, pitch, roll)
        axisAlignedBoundingBox.update(transformationMat)
    }

    override fun rotateYawBy(angle: Float) {
        super.rotateYawBy(angle)
        axisAlignedBoundingBox.update(transformationMat)
    }
}
