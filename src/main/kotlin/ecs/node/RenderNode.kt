package ecs.node

import Entity
import ecs.component.ModelComponent
import ecs.component.TransformComponent

data class RenderNode(
    override val entityId: String,
    val transformComponent: TransformComponent,
    val modelComponent: ModelComponent
) : BaseNode() {

    companion object {
        fun fromEntity(entity: Entity): RenderNode? {
            val tc = entity.getComponent(TransformComponent::class.java.name) as TransformComponent?
            val mc = entity.getComponent(ModelComponent::class.java.name) as ModelComponent?

            return if (tc != null && mc != null) {
                RenderNode(entity.id, tc, mc)
            } else {
                null
            }
        }
    }

}
