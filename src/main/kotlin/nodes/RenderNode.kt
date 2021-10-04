package nodes

import Entity
import components.ModelComponent
import components.TransformComponent
import nodes.core.BaseNode

data class RenderNode(
    override val entityId: String,
    val transformComponent: TransformComponent,
    val modelComponent: ModelComponent
) : BaseNode() {

    companion object {
        fun fromEntity(entity: Entity): RenderNode? {
            val pc = entity.getComponent(TransformComponent::class.java.name) as TransformComponent?
            val mc = entity.getComponent(ModelComponent::class.java.name) as ModelComponent?

            return if (pc != null && mc != null) {
                RenderNode(entity.id, pc, mc)
            } else {
                null
            }
        }
    }

}
