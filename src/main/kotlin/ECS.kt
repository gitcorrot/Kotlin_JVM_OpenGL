import nodes.MoveNode
import nodes.core.BaseNode
import systems.core.BaseSystem
import utils.Debug

class ECS {
    companion object {
        val TAG: String = this::class.java.name
    }

    private val entities = mutableSetOf<Entity>()
    private val systems = mutableSetOf<BaseSystem>()
    private val nodes = mutableMapOf<String, MutableList<Any>>() // ex. [ "MoveNode", [MoveNode, MoveNode, MoveNode] ]

    fun addEntity(entity: Entity) {
        Debug.logi(TAG, "Entity added! ($entity)")

        entities.add(entity)

        // create nodes from this entity's components
        MoveNode.fromEntity(entity)?.let { moveNode ->
            if (nodes[MoveNode::class.java.name] == null) {
                nodes[MoveNode::class.java.name] = mutableListOf()
            }
            nodes[MoveNode::class.java.name]!!.add(moveNode)
        }

        // TODO: observe entity if any component was added/removed
    }

    fun removeEntity(entity: Entity) {
        // remove all nodes that was created from entity
        for (k in nodes.keys) {
            nodes[k]?.removeAll { (it as BaseNode).entityId == entity.id }
        }
        entities.remove(entity)
        Debug.logi(TAG, "Entity removed! ($entity)")
    }

    fun addSystem(system: BaseSystem) {
        Debug.logi(TAG, "System added! ($system)")
        systems.add(system)
        system.start()
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
        Debug.logi(TAG, "ECS update")
        for (s in systems) {
            s.update(deltaTime)
        }
    }
}