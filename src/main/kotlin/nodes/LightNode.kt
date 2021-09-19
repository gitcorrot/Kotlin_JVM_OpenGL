package nodes

import Entity
import components.LightComponent
import nodes.core.BaseNode

data class LightNode(
    override val entityId: String,
    val lightComponent: LightComponent
) : BaseNode() {

    companion object {
        fun fromEntity(entity: Entity): LightNode? {
            val lc = entity.getComponent(LightComponent::class.java.name) as LightComponent?

            return if (lc != null) {
                LightNode(entity.id, lc)
            } else {
                null
            }
        }
    }

}
