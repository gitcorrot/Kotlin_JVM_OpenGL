package systems.core

abstract class BaseSystem {

    var isStarted: Boolean = false

    fun start() {
        isStarted = true
    }

    abstract fun update(deltaTime: Float)

    fun stop() {
        isStarted = false
    }
}