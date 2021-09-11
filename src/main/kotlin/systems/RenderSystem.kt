package systems

import systems.core.BaseSystem
import utils.Debug

object RenderSystem : BaseSystem() {
    val TAG: String = this::class.java.name

//    private var targets = listOf<RenderNode>()

    override fun update(deltaTime: Float) {
        Debug.logi(TAG, "update (deltaTime=$deltaTime)")
        if (!isStarted) return

        // TODO: render
    }
}