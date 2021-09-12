import components.PositionComponent
import components.VelocityComponent
import glm_.glm
import glm_.quat.Quat
import glm_.vec3.Vec3
import nodes.MoveNode
import systems.PhysicsSystem
import systems.RenderSystem
import utils.Debug

fun main() {
    Debug.DEBUG_LEVEL = Debug.DebugLevel.DEBUG

    Debug.logi("main.kt", "App started!")

    val engine = Engine()
    engine.run()

    Debug.logi("main.kt", "App finished!")
}
