package ecs

import Entity
import ecs.node.*
import ecs.system.*
import utils.Debug
import kotlin.system.measureNanoTime

class ECS {
    val TAG: String = this::class.java.name

    private val entities = mutableSetOf<Entity>()
    private val systems = mutableSetOf<BaseSystem>()
    private val nodes = mutableMapOf<String, MutableList<Any>>(
        CameraNode::class.java.name to mutableListOf(),
        LightNode::class.java.name to mutableListOf(),
        RenderNode::class.java.name to mutableListOf(),
        MoveNode::class.java.name to mutableListOf(),
        CollisionNode::class.java.name to mutableListOf()
    )

    fun addEntity(entity: Entity) {
        Debug.logd(TAG, "Entity added! ($entity)")

        // add entity
        entities.add(entity)

        // create all available nodes from this entity's components
        // and add then to proper systems
        CameraNode.fromEntity(entity)?.let { cameraNode ->
            nodes[CameraNode::class.java.name]!!.add(cameraNode)
            getSystemOfClass<InputSystem>()?.cameraNodes?.add(cameraNode)
            getSystemOfClass<DynamicFovSystem>()?.cameraNodes?.add(cameraNode)
            getSystemOfClass<RenderSystem>()?.cameraNodes?.add(cameraNode)
        }
        LightNode.fromEntity(entity)?.let { lightNode ->
            nodes[LightNode::class.java.name]!!.add(lightNode)
            getSystemOfClass<RenderSystem>()?.lightNodes?.add(lightNode)
        }
        MoveNode.fromEntity(entity)?.let { moveNode ->
            nodes[MoveNode::class.java.name]!!.add(moveNode)
            getSystemOfClass<MoveSystem>()?.moveNodes?.add(moveNode)
        }
        RenderNode.fromEntity(entity)?.let { renderNode ->
            nodes[RenderNode::class.java.name]!!.add(renderNode)
            getSystemOfClass<RenderSystem>()?.renderNodes?.add(renderNode)
        }
        CollisionNode.fromEntity(entity)?.let { collisionNode ->
            nodes[CollisionNode::class.java.name]!!.add(collisionNode)
            getSystemOfClass<RenderSystem>()?.collisionNodes?.add(collisionNode)
            getSystemOfClass<CollisionSystem>()?.collisionNodes?.add(collisionNode)
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
                        DynamicFovSystem.cameraNodes.remove(nodeToRemove)
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

    fun addSystem(system: BaseSystem, autoStart: Boolean = false) {
        Debug.logi(TAG, "System added! ($system)")
        systems.add(system)
        if (autoStart) {
            system.start()
        }
    }

    private inline fun <reified T : Any> getSystemOfClass(): T? {
        return systems.find { it.javaClass.name == T::class.java.name } as? T
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
        Debug.logi(TAG, "-------------------------- Update --------------------------")
        var totalTime = 0f
        for (s in systems) {
            try {
                val updateTime = measureNanoTime {
                    s.update(deltaTime)
                }
                totalTime += updateTime / 1000000f
                Debug.logd(TAG, "${s.javaClass.name} update time:\t\t\t%.3f ms".format(updateTime / 1000000f))
            } catch (e: Exception) {
                Debug.loge(TAG, e.localizedMessage)
                e.printStackTrace()
            }
        }
        Debug.logi(TAG, "Total update time: %.3f ms %s".format(totalTime, if (totalTime < 16f) "✔️" else "❌️"))
        println()
    }

    fun cleanup() {
        // TODO
    }
}