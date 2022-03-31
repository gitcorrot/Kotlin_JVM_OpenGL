import glm_.vec2.Vec2i
import utils.Debug

fun main() {
    Debug.DEBUG_LEVEL = Debug.DebugLevel.DEBUG
//    Debug.DEBUG_LEVEL = Debug.DebugLevel.INFO
//    Debug.DEBUG_LEVEL = Debug.DebugLevel.ERROR

    Debug.logi("main.kt", "App started!")

    val engine = Engine(
        windowSize = Vec2i(2000, 1500),
        vSyncEnabled = true,
    )
    engine.run()

    Debug.logi("main.kt", "App finished! \uD83D\uDC4B")
}
