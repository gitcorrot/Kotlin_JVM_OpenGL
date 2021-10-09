import utils.Debug
import java.util.*

data class Entity(
    val id: String = UUID.randomUUID().toString()
) {
    private val TAG: String = this::class.java.name
    private val components = mutableMapOf<String, Any>()

    fun addComponent(component: Any) = apply {
        Debug.logi(TAG, "Added ${component::class.java.name} to Entity id=$id!")
        components[component::class.java.name] = component
    }

    fun getComponent(className: String): Any? {
        return components[className]
    }

    fun getAllComponents(): List<String> {
        return components.keys.toList()
    }
}