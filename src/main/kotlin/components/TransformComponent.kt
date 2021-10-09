package components

import data.Movable
import data.Rotatable
import data.Scalable
import glm_.mat4x4.Mat4

data class TransformComponent(
    var movable: Movable = Movable(),
    var rotatable: Rotatable = Rotatable(),
    var scalable: Scalable = Scalable()
) {
    fun getTransformationMat() = Mat4(1f)
        .translate(movable.position)
        .times(rotatable.rotation.toMat4())
        .scale_(scalable.scale)
}