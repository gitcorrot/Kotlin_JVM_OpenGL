package nodes

import Entity
import components.PositionComponent
import nodes.core.BaseNode

data class RenderNode(
    override val entityId: String,
//    val positionComponent: PositionComponent,
    val positionComponent: PositionComponent,
) : BaseNode() {
    companion object {
        fun fromEntity(entity: Entity): RenderNode? {
            val pc = entity.getComponent(PositionComponent::class.java.name) as PositionComponent?

            return if (pc != null) {
                RenderNode(entity.id, pc)
            } else {
                null
            }
        }
    }
}