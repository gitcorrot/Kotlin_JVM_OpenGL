package collision

import data.Mesh
import data.Vertex
import glm_.vec2.Vec2
import glm_.vec3.Vec3

object BoundingBoxUtils {

    fun createMesh(boundingPoints: BoundingPoints, uvs: Vec2): Mesh {
        val vertices = arrayListOf(
            Vertex(Vec3(boundingPoints.minX, boundingPoints.minY, boundingPoints.maxZ), null, null, uvs),
            Vertex(Vec3(boundingPoints.maxX, boundingPoints.minY, boundingPoints.maxZ), null, null, uvs),
            Vertex(Vec3(boundingPoints.maxX, boundingPoints.maxY, boundingPoints.maxZ), null, null, uvs),
            Vertex(Vec3(boundingPoints.minX, boundingPoints.maxY, boundingPoints.maxZ), null, null, uvs),
            Vertex(Vec3(boundingPoints.minX, boundingPoints.minY, boundingPoints.minZ), null, null, uvs),
            Vertex(Vec3(boundingPoints.maxX, boundingPoints.minY, boundingPoints.minZ), null, null, uvs),
            Vertex(Vec3(boundingPoints.maxX, boundingPoints.maxY, boundingPoints.minZ), null, null, uvs),
            Vertex(Vec3(boundingPoints.minX, boundingPoints.maxY, boundingPoints.minZ), null, null, uvs),
        )

        val indices = intArrayOf(
            0, 1,
            1, 2,
            2, 3,
            3, 0,
            4, 5,
            5, 6,
            6, 7,
            7, 4,
            0, 4,
            1, 5,
            2, 6,
            3, 7
        )

        return Mesh(vertices, indices)
    }

    fun calculateBoundingPoints(vertices: ArrayList<Vertex>): BoundingPoints {
        val bp = BoundingPoints()

        vertices.forEach { v ->
            if (v.position.x < bp.minX) bp.minX = v.position.x
            if (v.position.y < bp.minY) bp.minY = v.position.y
            if (v.position.z < bp.minZ) bp.minZ = v.position.z

            if (v.position.x > bp.maxX) bp.maxX = v.position.x
            if (v.position.y > bp.maxY) bp.maxY = v.position.y
            if (v.position.z > bp.maxZ) bp.maxZ = v.position.z
        }
        return bp
    }
}