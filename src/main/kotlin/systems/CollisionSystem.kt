package systems

import nodes.CollisionNode
import systems.core.BaseSystem
import utils.Debug

object CollisionSystem : BaseSystem() {
    val TAG: String = this::class.java.name

    var collisionNodes = mutableListOf<CollisionNode>()

    override fun update(deltaTime: Float) {
        if (!isStarted) return
        Debug.logd(TAG, "update (deltaTime=$deltaTime)")

        for (collisionNode in collisionNodes) {
//            collisionNode.collisionComponent.
        }
    }
}