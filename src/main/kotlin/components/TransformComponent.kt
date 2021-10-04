package components

import data.Movable
import data.Rotatable
import data.Scalable

data class TransformComponent(
    var movable: Movable = Movable(),
    var rotatable: Rotatable = Rotatable(),
    var scalable: Scalable = Scalable()
)