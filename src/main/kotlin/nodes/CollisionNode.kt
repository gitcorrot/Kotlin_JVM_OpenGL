package nodes

import Entity
import components.CollisionComponent
import components.ModelComponent
import nodes.core.BaseNode

data class CollisionNode(
    override val entityId: String,
    val collisionComponent: CollisionComponent,
    val modelComponent: ModelComponent
) : BaseNode() {

    companion object {
        fun fromEntity(entity: Entity): CollisionNode? {
            val cc = entity.getComponent(CollisionComponent::class.java.name) as CollisionComponent?
            val mc = entity.getComponent(ModelComponent::class.java.name) as ModelComponent?

            return if (cc != null && mc != null) {
                CollisionNode(entity.id, cc, mc)
            } else {
                null
            }
        }
    }
}