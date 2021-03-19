package model

import glm_.vec2.Vec2
import glm_.vec3.Vec3

data class Vertex(
    val position: Vec3,
    val normal: Vec3,
    val textureCoordinates: Vec2
) {
    fun convertToFloatArray(): FloatArray {
        return floatArrayOf(
            position.x, position.y, position.z,
            normal.x, normal.y, normal.z,
            textureCoordinates.x, textureCoordinates.y
        )
    }
}