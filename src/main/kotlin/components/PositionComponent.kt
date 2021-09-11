package components

import glm_.quat.Quat
import glm_.vec3.Vec3

data class PositionComponent(
    var position: Vec3 = Vec3(),
    var rotation: Quat = Quat()
)