import utils.Debug

fun main() {
    Debug.DEBUG_LEVEL = Debug.DebugLevel.DEBUG

    Debug.logi("main.kt", "App started!")

    val engine = Engine()
    engine.run()

    Debug.logi("main.kt", "App finished!")
}
