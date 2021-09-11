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


    val posComponent = PositionComponent()
    posComponent.position = Vec3(5f, 5f, 5f)
    posComponent.rotation = Quat(glm.radians(Vec3(45f, 0f, 0f)))

    val velComponent = VelocityComponent()
    velComponent.velocity = Vec3(1f, 0f, 0f)

    val testEntity = Entity()
    testEntity.addComponent(posComponent)
    testEntity.addComponent(velComponent)

    val moveNode = MoveNode(
        testEntity.id,
        testEntity.getComponent(PositionComponent::class.java.name) as PositionComponent,
        testEntity.getComponent(VelocityComponent::class.java.name) as VelocityComponent
    )

    moveNode.positionComponent.position.y = 123f
    moveNode.velocityComponent.velocity.y = 234f
    println("-----------------------")
    println(posComponent)
    println(velComponent)
    println(moveNode)
    println("-----------------------")

    val ecs = ECS()
    ecs.addEntity(testEntity)
    ecs.addSystem(PhysicsSystem)
    ecs.addSystem(RenderSystem)
    ecs.update(16.6f)

    println(ecs.getNodesOfClass(MoveNode::class.java.name))

    ecs.removeEntity(testEntity)
    println(ecs.getNodesOfClass(MoveNode::class.java.name))




    return


    val engine = Engine()
    engine.run()

    Debug.logi("main.kt", "App finished!")
}
