import glm_.vec2.Vec2
import glm_.vec3.Vec3
import model.Mesh
import model.Vertex
import org.lwjgl.assimp.AIMesh
import org.lwjgl.assimp.Assimp

object ModelLoader {
    private val TAG: String = this::class.java.name

    @Throws
    fun loadStaticModel(path: String): Model {

        // For now it only handles single-mesh objects
        val scene = Assimp.aiImportFile(path, Assimp.aiProcess_JoinIdenticalVertices)
        scene ?: throw Exception("Scene loading failed! Check path: $path")

        if (scene.mNumMeshes() == 1) {
            val mesh = processMesh(AIMesh.create(scene.mMeshes()!!.get()))
            val model = Model()
            model.create(mesh)
            return model
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

        normalsBuffer ?: throw Exception("Normals buffer is null!")
        uvsBuffer ?: throw Exception("Texture coordinates buffer is null!")

        for (i in 0 until mesh.mNumVertices()) {
            val vertex = Vertex(Vec3(), Vec3(), Vec2())
            val position = positionsBuffer.get()

            vertex.position.x = position.x()
            vertex.position.y = position.y()
            vertex.position.z = position.z()

            val normal = normalsBuffer.get()
            vertex.normal.x = normal.x()
            vertex.normal.y = normal.y()
            vertex.normal.z = normal.z()

            val uv = uvsBuffer.get()
            vertex.textureCoordinates.x = uv.x()
            vertex.textureCoordinates.y = uv.y()

            vertices.add(vertex)
        }

        Debug.logi(TAG, "${vertices.size} vertices loaded!")
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

        Debug.logi(TAG, "${indices.size} indices loaded!")
        return indices
    }
}