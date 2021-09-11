package nodes

import Entity
import components.PositionComponent
import components.VelocityComponent
import nodes.core.BaseNode

data class MoveNode(
    val mEntityId: String,
    val positionComponent: PositionComponent,
    val velocityComponent: VelocityComponent
) : BaseNode(mEntityId) {
    companion object {
        fun fromEntity(entity: Entity): MoveNode? {
            val pc = entity.getComponent(PositionComponent::class.java.name) as PositionComponent?
            val vc = entity.getComponent(VelocityComponent::class.java.name) as VelocityComponent?

            return if (pc != null && vc != null) {
                MoveNode(entity.id, pc, vc)
            } else {
                null
            }
        }
    }
}