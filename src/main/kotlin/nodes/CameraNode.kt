package nodes

import Entity
import components.CameraComponent
import components.PositionComponent
import nodes.core.BaseNode

data class CameraNode(
    override val entityId: String,
    val positionComponent: PositionComponent,
    val cameraComponent: CameraComponent
) : BaseNode() {

    companion object {
        fun fromEntity(entity: Entity): CameraNode? {
            val pc = entity.getComponent(PositionComponent::class.java.name) as PositionComponent?
            val cc = entity.getComponent(CameraComponent::class.java.name) as CameraComponent?

            return if (pc != null && cc != null) {
                CameraNode(entity.id, pc, cc)
            } else {
                null
            }
        }
    }

}
