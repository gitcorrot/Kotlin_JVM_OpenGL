package utils

import data.Mesh
import data.Vertex
import glm_.glm
import glm_.vec2.Vec2
import glm_.vec3.Vec3

object TerrainUtils {

    fun generateMesh(size: Int, tileSize: Float, a: Float): Mesh {

        val mesh = Mesh(ArrayList())

        for (z in 0..size) {
            for (x in 0..size) {
                val tilePosition = Vec3()
                tilePosition.x = x * tileSize
                tilePosition.y = glm.simplex(Vec2(z * a, x * a)) * tileSize
                tilePosition.z = -z * tileSize

                val tileColor = Vec3(0.4f, 0.6f, 0.3f) // TODO

                mesh.vertices.add(
                    Vertex(
                        position = tilePosition,
                        normal = null, // calculated in geometry shader
                        color = tileColor,
                        textureCoordinates = null
                    )
                )
            }
        }

        // Calculate indices
        val indices = arrayListOf<Int>()

        //   (z+1,x)     (z+1,x+1)
        //      |------- -|
        //      |      -  |
        //      |    -    |
        //      |  -      |
        //      |- ------ |
        //   (z,x)      (z,x+1)
        for (z in 0 until size) {
            for (x in 0 until size) {
                val lb = z * (size + 1) + x
                val rb = z * (size + 1) + x + 1
                val lt = (z + 1) * (size + 1) + x
                val rt = (z + 1) * (size + 1) + x + 1

                // triangle 1 -> lb, rt, lt
                indices.add(lt)
                indices.add(lb)
                indices.add(rt)

                // triangle 2 -> lb, rb, rt
                indices.add(rb)
                indices.add(rt)
                indices.add(lb)
            }
        }
        mesh.indices = indices.toIntArray()

        return mesh
    }

}