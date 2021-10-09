package ecs.system

import ecs.node.MoveNode

object MoveSystem : BaseSystem() {
    val TAG: String = this::class.java.name

    var moveNodes = mutableListOf<MoveNode>()

    override fun update(deltaTime: Float) {
        if (!isStarted) return
//        Debug.logd(TAG, "update (deltaTime=$deltaTime)")

        for (moveNode in moveNodes) {
            val velocity = moveNode.velocityComponent.velocity * deltaTime
            moveNode.transformComponent.movable.moveBy(velocity)
        }
    }
}