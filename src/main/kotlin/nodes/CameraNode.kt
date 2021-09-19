package nodes

import Entity
import components.CameraComponent
import nodes.core.BaseNode

data class CameraNode(
    override val entityId: String,
    val cameraComponent: CameraComponent
) : BaseNode() {

    companion object {
        fun fromEntity(entity: Entity): CameraNode? {
            val cc = entity.getComponent(CameraComponent::class.java.name) as CameraComponent?

            return if (cc != null) {
                CameraNode(entity.id, cc)
            } else {
                null
            }
        }
    }

}
