package utils

import data.Mesh
import data.Vertex
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.assimp.AIMesh
import org.lwjgl.assimp.Assimp

object ModelLoader {
    private val TAG: String = this::class.java.name

    @Throws
    fun loadStaticModel(path: String): Mesh {
        // For now it only handles single-mesh objects
        val scene = Assimp.aiImportFile(path, Assimp.aiProcess_JoinIdenticalVertices)
        scene ?: throw Exception("Scene loading failed! Check path: $path")

        if (scene.mNumMeshes() == 1) {
            return processMesh(AIMesh.create(scene.mMeshes()!!.get()))
        } else {
            throw Exception("Meshes size: ${scene.mNumMeshes()}, not 1!")
        }
    }

    private fun processMesh(mesh: AIMesh): Mesh {
        val vertices = getVertices(mesh)
        val indices = getIndices(mesh)

        return Mesh(vertices, indices)
    }

    @Throws
    private fun getVertices(mesh: AIMesh): ArrayList<Vertex> {
        val vertices: ArrayList<Vertex> = ArrayList()

        val positionsBuffer = mesh.mVertices()
        val normalsBuffer = mesh.mNormals()
        val uvsBuffer = mesh.mTextureCoords(0)

        normalsBuffer ?: Debug.logd(TAG, "Normals buffer is null!")
        uvsBuffer ?: throw Exception("Texture coordinates buffer is null!")

        for (i in 0 until mesh.mNumVertices()) {
            val vertex = if (normalsBuffer != null)
                Vertex(Vec3(), Vec3(), null, Vec2())
            else
                Vertex(Vec3(), null, null, Vec2())

            val position = positionsBuffer.get()
            vertex.position.x = position.x()
            vertex.position.y = position.y()
            vertex.position.z = position.z()

            // Only when normals exists
            vertex.normal?.let {
                val normal = normalsBuffer!!.get()
                it.x = normal.x()
                it.y = normal.y()
                it.z = normal.z()
            }

            val uv = uvsBuffer.get()
            vertex.textureCoordinates!!.x = uv.x()
            vertex.textureCoordinates!!.y = uv.y()

            vertices.add(vertex)
        }

        Debug.logd(TAG, "${vertices.size} vertices loaded!")
        return vertices
    }

    private fun getIndices(mesh: AIMesh): IntArray {
        val indices = IntArray(mesh.mNumFaces() * 3) // number of indices = (number of faces * 3)
        val facesBuffer = mesh.mFaces()

        var index = 0
        while (facesBuffer.remaining() > 0) {
            val face = facesBuffer.get()
            val faceIndicesBuffer = face.mIndices()

            while (faceIndicesBuffer.remaining() > 0) {
                indices[index++] = faceIndicesBuffer.get()
            }
        }

        Debug.logd(TAG, "${indices.size} indices loaded!")
        return indices
    }
}