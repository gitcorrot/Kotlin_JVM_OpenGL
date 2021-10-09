package systems

import Texture
import collision.BoundingBoxUtils
import components.CollisionComponentType
import glm_.vec2.Vec2
import models.base.ModelNoLight
import nodes.CollisionNode
import systems.core.BaseSystem
import utils.Debug

object CollisionSystem : BaseSystem() {
    val TAG: String = this::class.java.name

    var collisionNodes = mutableListOf<CollisionNode>()

    override fun update(deltaTime: Float) {
        if (!isStarted) return
//        Debug.logd(TAG, "update (deltaTime=$deltaTime)")

        for (collisionNode in collisionNodes) {
            if (!collisionNode.collisionComponent.isInitialized) {
                initializeCollisionComponent(collisionNode)
            } else {
                updateCollisionComponent(collisionNode)
            }
        }
    }

    private fun initializeCollisionComponent(collisionNode: CollisionNode) {
        when (collisionNode.collisionComponent.type) {
            CollisionComponentType.AXIS_ALIGNED -> {
                val modelVertices = collisionNode.modelComponent.model.mesh.vertices
                val boundingPoints = BoundingBoxUtils.calculateBoundingPoints(modelVertices)
                val primaryMesh = BoundingBoxUtils.createMesh(boundingPoints, Vec2(0f, 0f))

                collisionNode.collisionComponent.boundingPoints = boundingPoints
                collisionNode.collisionComponent.primaryMesh = primaryMesh
                collisionNode.collisionComponent.boundingBoxModel =
                    ModelNoLight(primaryMesh, Texture.getDefaultColorPalette())
                collisionNode.collisionComponent.isInitialized = true
            }
            CollisionComponentType.OBJECT_ORIENTED -> TODO()
        }
    }

    private fun updateCollisionComponent(collisionNode: CollisionNode) {
        when (collisionNode.collisionComponent.type) {
            CollisionComponentType.AXIS_ALIGNED -> {
                val transformationMat = collisionNode.transformComponent.getTransformationMat()

                // Calculate oriented bounding box
                val transformedVertices = BoundingBoxUtils.calculateTransformedMeshVertices(
                    collisionNode.collisionComponent.primaryMesh, transformationMat
                )

                // Calculate axis aligned bounding box over oriented one
                val newBoundingPoints = BoundingBoxUtils.calculateBoundingPoints(transformedVertices)
                val newBoundingMesh = BoundingBoxUtils.createMesh(newBoundingPoints, Vec2(0f, 0f))
                collisionNode.collisionComponent.boundingPoints = newBoundingPoints
                collisionNode.collisionComponent.boundingBoxModel.uploadVertices(newBoundingMesh)
            }
            CollisionComponentType.OBJECT_ORIENTED -> TODO()
        }
    }
}