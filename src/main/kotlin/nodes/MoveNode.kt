package nodes

import Entity
import components.TransformComponent
import components.VelocityComponent
import nodes.core.BaseNode

data class MoveNode(
    override val entityId: String,
    val transformComponent: TransformComponent,
    val velocityComponent: VelocityComponent
) : BaseNode() {

    // tu mozna zmienic na List<Any> w base klasie zeby latwiej iterowac po komponentach
//    val components: List<Any> = listOf(positionComponent, positionComponent)

    companion object {
        fun fromEntity(entity: Entity): MoveNode? {
            val tc = entity.getComponent(TransformComponent::class.java.name) as TransformComponent?
            val vc = entity.getComponent(VelocityComponent::class.java.name) as VelocityComponent?

            return if (tc != null && vc != null) {
                MoveNode(entity.id, tc, vc)
            } else {
                null
            }
        }
    }
}