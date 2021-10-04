package collision

import data.Mesh
import data.Vertex
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

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

    fun calculateBoundingPoints(vertices: List<Vertex>): BoundingPoints {
        return BoundingPoints().apply {
            vertices.forEach { v ->
                if (v.position.x < this.minX) this.minX = v.position.x
                if (v.position.y < this.minY) this.minY = v.position.y
                if (v.position.z < this.minZ) this.minZ = v.position.z

                if (v.position.x > this.maxX) this.maxX = v.position.x
                if (v.position.y > this.maxY) this.maxY = v.position.y
                if (v.position.z > this.maxZ) this.maxZ = v.position.z
            }
        }

//        val bp = BoundingPoints()
//
//        vertices.forEach { v ->
//            if (v.position.x < bp.minX) bp.minX = v.position.x
//            if (v.position.y < bp.minY) bp.minY = v.position.y
//            if (v.position.z < bp.minZ) bp.minZ = v.position.z
//
//            if (v.position.x > bp.maxX) bp.maxX = v.position.x
//            if (v.position.y > bp.maxY) bp.maxY = v.position.y
//            if (v.position.z > bp.maxZ) bp.maxZ = v.position.z
//        }
//        return bp
    }

    fun calculateTransformedMeshVertices(mesh: Mesh, transformationMat: Mat4): List<Vertex> {
        return mesh.vertices.map { vertex ->
            val transformedPosition = transformationMat.times(Vec4(vertex.position, 1f)).toVec3()
            Vertex(transformedPosition, null, null, AxisAlignedBoundingBox.uvs)
        }

//        val transformedVertices = ArrayList<Vertex>()
//        mesh.vertices.forEach { vertex ->
//            val transformedPosition = transformationMat.times(Vec4(vertex.position, 1f)).toVec3()
//            transformedVertices.add(Vertex(transformedPosition, null, null, AxisAlignedBoundingBox.uvs))
//        }
    }
}