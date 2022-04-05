import ecs.node.*
import ecs.system.*
import org.koin.dsl.module

class CameraNodes : MutableList<CameraNode> by mutableListOf()
class LightNodes : MutableList<LightNode> by mutableListOf()
class RenderNodes : MutableList<RenderNode> by mutableListOf()
class MoveNodes : MutableList<MoveNode> by mutableListOf()
class CollisionNodes : MutableList<CollisionNode> by mutableListOf()

val coreModules = module {
    single { AppSettings() }

    single { CameraNodes() }
    single { LightNodes() }
    single { RenderNodes() }
    single { MoveNodes() }
    single { CollisionNodes() }

    single { CollisionSystem() }
    single { DynamicFovSystem() }
    single { InputSystem() }
    single { MoveSystem() }
    single { RenderSystem() }
}
