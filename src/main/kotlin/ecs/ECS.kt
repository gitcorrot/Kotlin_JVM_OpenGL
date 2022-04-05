package ecs

import CameraNodes
import CollisionNodes
import Entity
import LightNodes
import MoveNodes
import RenderNodes
import ecs.node.*
import ecs.system.BaseSystem
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import utils.Debug
import kotlin.system.measureNanoTime

class ECS : KoinComponent {
    companion object {
        private val TAG: String = this::class.java.name
    }

    private val cameraNodes by inject<CameraNodes>()
    private val lightNodes by inject<LightNodes>()
    private val renderNodes by inject<RenderNodes>()
    private val moveNodes by inject<MoveNodes>()
    private val collisionNodes by inject<CollisionNodes>()

    private val entities = mutableSetOf<Entity>()
    private val systems = mutableSetOf<BaseSystem>()
    private val nodes = mutableMapOf(
        CameraNode::class.java.name to cameraNodes,
        LightNode::class.java.name to lightNodes,
        RenderNode::class.java.name to renderNodes,
        MoveNode::class.java.name to moveNodes,
        CollisionNode::class.java.name to collisionNodes
    )

    fun addEntity(entity: Entity) {
        entities.add(entity)

        CameraNode.fromEntity(entity)?.let { cameraNodes.add(it) }
        LightNode.fromEntity(entity)?.let { lightNodes.add(it) }
        RenderNode.fromEntity(entity)?.let { renderNodes.add(it) }
        MoveNode.fromEntity(entity)?.let { moveNodes.add(it) }
        CollisionNode.fromEntity(entity)?.let { collisionNodes.add(it) }

        Debug.logd(TAG, "Entity added! ($entity)")

        // TODO: observe entity if any component was added/removed
    }

    fun removeEntity(entity: Entity) {
        // remove all nodes that was created from entity
        for (k in nodes.keys) {
            val nodeToRemove = nodes[k]?.find { it.entityId == entity.id }

            if (nodeToRemove != null) {
                when (k) {
                    CameraNode::class.java.name -> cameraNodes.remove(nodeToRemove)
                    LightNode::class.java.name -> lightNodes.remove(nodeToRemove)
                    RenderNode::class.java.name -> renderNodes.remove(nodeToRemove)
                    MoveNode::class.java.name -> moveNodes.remove(nodeToRemove)
                    CollisionNode::class.java.name -> collisionNodes.remove(nodeToRemove)
                }
                nodes[k]?.remove(nodeToRemove)
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