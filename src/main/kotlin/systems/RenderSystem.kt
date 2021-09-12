package systems

import nodes.RenderNode
import systems.core.BaseSystem
import utils.Debug

object RenderSystem : BaseSystem() {
    val TAG: String = this::class.java.name

    var targets = mutableListOf<RenderNode>()

    override fun start() {
        super.start()
    }

    override fun update(deltaTime: Float) {
        Debug.logi(TAG, "update (deltaTime=$deltaTime)")
        if (!isStarted) return

        // TODO: render
    }

    override fun stop() {
        super.stop()
    }
}