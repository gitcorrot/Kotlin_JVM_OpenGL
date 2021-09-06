import org.koin.core.context.startKoin
import utils.Debug

fun main() {
    Debug.DEBUG_LEVEL = Debug.DebugLevel.DEBUG

    Debug.logi("main.kt", "App started!")

    startKoin {
        printLogger()
        modules(
            cameraModule,
            worldModule,
            inputManagerModule
        )
    }

    val engine = Engine()
    engine.run()

    Debug.logi("main.kt", "App finished!")
}
