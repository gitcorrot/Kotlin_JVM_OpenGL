package components

import collision.BoundingPoints
import data.Mesh
import models.base.ModelNoLight

enum class CollisionComponentType {
    AXIS_ALIGNED, OBJECT_ORIENTED
}

data class CollisionComponent(
    val type: CollisionComponentType,
    var isInitialized: Boolean = false
) {
    lateinit var boundingPoints: BoundingPoints
    lateinit var primaryMesh: Mesh
    lateinit var boundingBoxModel: ModelNoLight
}