import glm_.vec2.Vec2i
import utils.Debug

fun main() {
    Debug.DEBUG_LEVEL = Debug.DebugLevel.DEBUG
//    Debug.DEBUG_LEVEL = Debug.DebugLevel.INFO
//    Debug.DEBUG_LEVEL = Debug.DebugLevel.ERROR

    Debug.logi("main.kt", "App started!")

    val engine = Engine(
        windowSize = Vec2i(1000, 800),
        vSyncEnabled = true,
    )
    engine.run()

    Debug.logi("main.kt", "App finished!")
}
