package data

import glm_.mat4x4.Mat4
import glm_.vec3.Vec3

data class Transformation(
    var translation: Vec3 = Vec3(0f, 0f, 0f),
    var rotation: Mat4 = Mat4(1f),
    var scale: Vec3 = Vec3(1f),
)