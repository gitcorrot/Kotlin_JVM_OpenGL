package model

import glm_.vec2.Vec2
import glm_.vec3.Vec3

data class Vertex(
    val position: Vec3,
    val normal: Vec3?,
    val textureCoordinates: Vec2
) {
    /**
     * @return if normal is not null returns 8 floats long array, otherwise 5 floats long array
     */
    fun convertToFloatArray(): FloatArray {
        return if (normal == null) {
            floatArrayOf(
                position.x, position.y, position.z,
                textureCoordinates.x, textureCoordinates.y
            )
        } else {
            floatArrayOf(
                position.x, position.y, position.z,
                normal.x, normal.y, normal.z,
                textureCoordinates.x, textureCoordinates.y
            )
        }
    }
}