package components

import collision.BoundingPoints
import data.Mesh
import models.base.ModelNoLight

enum class BoundingBoxType {
    AXIS_ALIGNED, OBJECT_ORIENTED
}

/**
 * @param optimize if set to true, bounding box will be calculated based on oriented bounding box (faster),
 * else bounding box will be calculated based on model vertices (more accurate, but also more heavy)
 *
 */
data class CollisionComponent(
    val boundingBoxType: BoundingBoxType,
    val optimize: Boolean = true,
    var isInitialized: Boolean = false
) {
    lateinit var boundingPoints: BoundingPoints
    lateinit var primaryMesh: Mesh
    lateinit var boundingBoxModel: ModelNoLight
}