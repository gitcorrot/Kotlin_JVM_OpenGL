package components

import data.Mesh

enum class CollisionComponentType {
    AXIS_ALIGNED, OBJECT_ORIENTED
}

data class CollisionComponent(
    val modelMesh: Mesh,
    val type: CollisionComponentType
)