package systems.core

abstract class BaseSystem {

    var isStarted: Boolean = false

    open fun start() {
        isStarted = true
    }

    abstract fun update(deltaTime: Float)

    open fun stop() {
        isStarted = false
    }
}