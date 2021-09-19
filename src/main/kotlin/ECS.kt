import nodes.CameraNode
import nodes.LightNode
import nodes.MoveNode
import nodes.RenderNode
import nodes.core.BaseNode
import systems.InputSystem
import systems.MoveSystem
import systems.RenderSystem
import systems.core.BaseSystem
import utils.Debug

class ECS {
    val TAG: String = this::class.java.name

    private val entities = mutableSetOf<Entity>()
    private val systems = mutableSetOf<BaseSystem>()
    private val nodes = mutableMapOf<String, MutableList<Any>>(
        CameraNode::class.java.name to mutableListOf(),
        LightNode::class.java.name to mutableListOf(),
        RenderNode::class.java.name to mutableListOf(),
        MoveNode::class.java.name to mutableListOf(),
    )

    fun addEntity(entity: Entity) {
        Debug.logi(TAG, "Entity added! ($entity)")

        // add entity
        entities.add(entity)

        // create all available nodes from this entity's components
        // and add then to proper systems
        CameraNode.fromEntity(entity)?.let { cameraNode ->
            nodes[CameraNode::class.java.name]!!.add(cameraNode)

            getSystemOfClass(InputSystem::class.java.name)?.let {
                (it as InputSystem).cameraNodes.add(cameraNode)
            }
            getSystemOfClass(RenderSystem::class.java.name)?.let {
                (it as RenderSystem).cameraNodes.add(cameraNode)
            }
        }
        LightNode.fromEntity(entity)?.let { lightNode ->
            nodes[LightNode::class.java.name]!!.add(lightNode)

            getSystemOfClass(RenderSystem::class.java.name)?.let {
                (it as RenderSystem).lightNodes.add(lightNode)
            }
        }
        MoveNode.fromEntity(entity)?.let { moveNode ->
            nodes[MoveNode::class.java.name]!!.add(moveNode)

            getSystemOfClass(MoveSystem::class.java.name)?.let {
                (it as MoveSystem).moveNodes.add(moveNode)
            }
        }
        RenderNode.fromEntity(entity)?.let { renderNode ->
            nodes[RenderNode::class.java.name]!!.add(renderNode)

            getSystemOfClass(RenderSystem::class.java.name)?.let {
                (it as RenderSystem).renderNodes.add(renderNode)
            }
        }

        // TODO: observe entity if any component was added/removed
    }

    fun removeEntity(entity: Entity) {
        // remove all nodes that was created from entity
        for (k in nodes.keys) {
            val nodeToRemove = nodes[k]?.find { (it as BaseNode).entityId == entity.id }

            if (nodeToRemove != null) {
                nodes[k]?.remove(nodeToRemove)

                when (k) {
                    CameraNode::class.java.name -> {
                        InputSystem.cameraNodes.remove(nodeToRemove)
                        RenderSystem.cameraNodes.remove(nodeToRemove)
                    }
                    RenderNode::class.java.name -> {
                        RenderSystem.renderNodes.remove(nodeToRemove)
                    }
                    LightNode::class.java.name -> {
                        RenderSystem.lightNodes.remove(nodeToRemove)
                    }
                    MoveNode::class.java.name -> {
                        MoveSystem.moveNodes.remove(nodeToRemove)
                    }
                }
            }
        }
        entities.remove(entity)

        Debug.logi(TAG, "Entity removed! ($entity)")
    }

    fun addSystem(system: BaseSystem) {
        Debug.logi(TAG, "System added! ($system)")
        systems.add(system)
        system.start()
    }

    private fun getSystemOfClass(systemClass: String): BaseSystem? {
        return systems.find { it::class.java.name == systemClass }
    }

    fun removeSystem(system: BaseSystem) {
        Debug.logi(TAG, "System removed! ($system)")
        system.stop()
        systems.remove(system)
    }

    fun getNodesOfClass(nodeClass: String): List<Any>? {
        return nodes[nodeClass]?.toList()
    }

    fun update(deltaTime: Float) {
        Debug.logd(TAG, "---------------------------------------------------------------------")
        Debug.logd(TAG, "ECS update")
        for (s in systems) {
            s.update(deltaTime)
        }
    }

    fun cleanup() {
        // TODO
    }
}