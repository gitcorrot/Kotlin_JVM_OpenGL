package ecs.system

import MoveNodes
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MoveSystem : BaseSystem(), KoinComponent {
    companion object {
        private val TAG: String = this::class.java.name
    }

    private val moveNodes by inject<MoveNodes>()

    override fun update(deltaTime: Float) {
        if (!isStarted) return
//        Debug.logd(TAG, "update (deltaTime=$deltaTime)")

        for (moveNode in moveNodes) {
            val velocity = moveNode.velocityComponent.velocity * deltaTime
            moveNode.transformComponent.movable.moveBy(velocity)
        }
    }
}