package nodes

import Entity
import components.CollisionComponent
import components.ModelComponent
import components.TransformComponent
import nodes.core.BaseNode

data class CollisionNode(
    override val entityId: String,
    val collisionComponent: CollisionComponent,
    val modelComponent: ModelComponent,
    val transformComponent: TransformComponent
) : BaseNode() {

    companion object {
        fun fromEntity(entity: Entity): CollisionNode? {
            val cc = entity.getComponent(CollisionComponent::class.java.name) as CollisionComponent?
            val mc = entity.getComponent(ModelComponent::class.java.name) as ModelComponent?
            val tc = entity.getComponent(TransformComponent::class.java.name) as TransformComponent?

            return if (cc != null && mc != null && tc != null) {
                CollisionNode(entity.id, cc, mc, tc)
            } else {
                null
            }
        }
    }
}