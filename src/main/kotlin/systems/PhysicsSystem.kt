package systems

import nodes.MoveNode
import systems.core.BaseSystem
import utils.Debug

object PhysicsSystem : BaseSystem() {
    val TAG: String = this::class.java.name

    var targets = mutableListOf<MoveNode>()

    override fun update(deltaTime: Float) {
        Debug.logi(TAG, "update (deltaTime=$deltaTime)")
        if (!isStarted) return

        for (t in targets) {
            println("TTTTTTTTTTTTTTTTTTT $t")
            t.positionComponent.position.plusAssign(t.velocityComponent.velocity)
        }
    }
}