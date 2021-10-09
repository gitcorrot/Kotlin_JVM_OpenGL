package systems

import Texture
import collision.BoundingBoxUtils
import components.BoundingBoxType
import glm_.vec2.Vec2
import models.base.ModelNoLight
import nodes.CollisionNode
import systems.core.BaseSystem

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
        when (collisionNode.collisionComponent.boundingBoxType) {
            BoundingBoxType.AXIS_ALIGNED -> {
                val modelVertices = collisionNode.modelComponent.model.mesh.vertices
                val boundingPoints = BoundingBoxUtils.calculateBoundingPoints(modelVertices)
                val primaryMesh = BoundingBoxUtils.createMesh(boundingPoints, Vec2(0f, 0f))

                collisionNode.collisionComponent.boundingPoints = boundingPoints
                collisionNode.collisionComponent.primaryMesh = primaryMesh
                collisionNode.collisionComponent.boundingBoxModel =
                    ModelNoLight(primaryMesh, Texture.getDefaultColorPalette())
                collisionNode.collisionComponent.isInitialized = true
            }
            BoundingBoxType.OBJECT_ORIENTED -> TODO()
        }
    }

    private fun updateCollisionComponent(collisionNode: CollisionNode) {
        when (collisionNode.collisionComponent.boundingBoxType) {
            BoundingBoxType.AXIS_ALIGNED -> {
                val transformationMat = collisionNode.transformComponent.getTransformationMat()

                // Calculate oriented bounding box
                val transformedVertices = when {
                    collisionNode.collisionComponent.optimize -> {
                        BoundingBoxUtils.calculateTransformedMeshVertices(
                            collisionNode.collisionComponent.primaryMesh, transformationMat
                        )
                    }
                    else -> {
                        BoundingBoxUtils.calculateTransformedMeshVertices(
                            collisionNode.modelComponent.model.mesh, transformationMat
                        )
                    }
                }

                // Calculate axis aligned bounding box over oriented one
                val newBoundingPoints = BoundingBoxUtils.calculateBoundingPoints(transformedVertices)
                val newBoundingMesh = BoundingBoxUtils.createMesh(newBoundingPoints, Vec2(0f, 0f))
                collisionNode.collisionComponent.boundingPoints = newBoundingPoints
                collisionNode.collisionComponent.boundingBoxModel.uploadVertices(newBoundingMesh)
            }
            BoundingBoxType.OBJECT_ORIENTED -> TODO()
        }
    }
}