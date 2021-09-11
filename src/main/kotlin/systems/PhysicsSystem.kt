package systems

import nodes.MoveNode
import systems.core.BaseSystem
import utils.Debug

object PhysicsSystem : BaseSystem() {
    val TAG: String = this::class.java.name

    private var targets = listOf<MoveNode>()


    override fun update(deltaTime: Float) {
        Debug.logi(TAG, "update (deltaTime=$deltaTime)")
        if (!isStarted) return

        for (t in targets) {
            t.positionComponent.position.plusAssign(t.velocityComponent.velocity)
        }
    }
}