import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import model.Mesh
import model.Transformation
import org.lwjgl.opengl.GL33
import org.lwjgl.opengl.GL33.glBindVertexArray
import org.lwjgl.opengl.GL33.glDeleteVertexArrays

open class Model(val mesh: Mesh) {

    var vao: Int = -1
        @Throws
        get() {
            if (field == -1)
                throw Exception("Model VAO id not assigned!")
            return field
        }

    var texture = Texture()
    val tranformation = Transformation()

    // It has to be overridden!
    // TODO: move to constructor and override?
    open fun create() {

    }


    fun getTransformationMat(): Mat4 = Mat4(1f)
        .translate(tranformation.translation)
        .times(tranformation.rotation)
        .scale_(tranformation.scale)

    fun moveBy(x: Float, y: Float, z: Float) {
        tranformation.translation.x += x;
        tranformation.translation.y += y;
        tranformation.translation.z += z;
    }

    fun moveTo(x: Float, y: Float, z: Float) {
        tranformation.translation.x = x;
        tranformation.translation.y = y;
        tranformation.translation.z = z;
    }

    fun rotate(pitch: Float, yaw: Float, roll: Float) {
        tranformation.rotation = Mat4()
            .rotate_(glm.radians(pitch), Vec3(1f, 0f, 0f))
            .rotate_(glm.radians(yaw), Vec3(0f, 1f, 0f))
            .rotate_(glm.radians(roll), Vec3(0f, 0f, 1f))
    }

    fun rotateBy(pitch: Float, yaw: Float, roll: Float) {
        tranformation.rotation
            .rotate_(glm.radians(pitch), Vec3(1f, 0f, 0f))
            .rotate_(glm.radians(yaw), Vec3(0f, 1f, 0f))
            .rotate_(glm.radians(roll), Vec3(0f, 0f, 1f))
    }

    fun scale(x: Float, y: Float, z: Float) {
        tranformation.scale.x = x;
        tranformation.scale.y = y;
        tranformation.scale.z = z;
    }

    fun getIndicesCount() = this.mesh?.indices?.size ?: 0

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
