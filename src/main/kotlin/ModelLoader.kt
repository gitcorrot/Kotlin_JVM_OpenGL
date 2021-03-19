import org.lwjgl.PointerBuffer
import org.lwjgl.assimp.AIMesh
import org.lwjgl.assimp.Assimp

data class LoadedModel(
    val meshes: ArrayList<Mesh>
)

class ModelLoader {
    private val TAG: String = this::class.java.name

    fun loadStaticModel(path: String): LoadedModel? {
        var loadedModel: LoadedModel? = null

        Assimp.aiImportFile(
            path,
            Assimp.aiProcess_Triangulate or Assimp.aiProcess_JoinIdenticalVertices
        )?.let { scene ->

            // MESHES
            val meshes = scene.mMeshes()
            meshes?.let {
                Debug.logi(TAG, "Meshes: ${scene.mNumMeshes()}")
                loadedModel = LoadedModel(processMeshes(it))
            }

            // MATERIALS
            val materials = scene.mMaterials()
            materials?.let {
                Debug.logi(TAG, "Materials: ${scene.mNumMaterials()}")
                processMaterials(it)
            }
        }

        return loadedModel
    }

    private fun processMeshes(meshes: PointerBuffer): ArrayList<Mesh> {
        val meshArrayList = ArrayList<Mesh>()

        while (meshes.remaining() > 0) {
            val mesh = processMesh(AIMesh.create(meshes.get()))
            meshArrayList.add(mesh)
        }

        return meshArrayList
    }

    private fun processMesh(mesh: AIMesh): Mesh {
        val vertices = getVertices(mesh)
//        println("${vertices.size} V: " + vertices.joinToString())

        val normals = getNormals(mesh)
//        println("${normals.size} N: " + normals.joinToString())

        val indices = getIndices(mesh)
//        println("${indices.size} I: " + indices.joinToString())

        val textureCoords = getTextureCoords(mesh)

        return Mesh(vertices, indices, normals, textureCoords)
    }

    private fun getVertices(mesh: AIMesh): FloatArray {
        val vertices = FloatArray(mesh.mNumVertices() * 3)
        val verticesBuffer = mesh.mVertices()

        var index = 0
        while (verticesBuffer.remaining() > 0) {
            val vertex = verticesBuffer.get()
            vertices[index++] = vertex.x()
            vertices[index++] = vertex.y()
            vertices[index++] = vertex.z()
        }

        return vertices
    }

    private fun getNormals(mesh: AIMesh): FloatArray {
        val normals = FloatArray(mesh.mNumVertices() * 3) // let's assume that number of normals == number of verties
        val normalsBuffer = mesh.mNormals()

        if (normalsBuffer != null) {
            var index = 0
            while (normalsBuffer.remaining() > 0) {
                val normal = normalsBuffer.get()
                normals[index++] = normal.x()
                normals[index++] = normal.y()
                normals[index++] = normal.z()
            }
        }

        return normals
    }

    private fun getIndices(mesh: AIMesh): IntArray {
        val indices = IntArray(mesh.mNumFaces() * 3) // let's assume that number of indices = (number of faces * 3)
        val facesBuffer = mesh.mFaces()

        var index = 0
        while (facesBuffer.remaining() > 0) {
            val face = facesBuffer.get()
            val faceIndicesBuffer = face.mIndices()

            while (faceIndicesBuffer.remaining() > 0) {
                indices[index++] = faceIndicesBuffer.get()
            }
        }

        return indices
    }

    private fun getTextureCoords(mesh: AIMesh): FloatArray {
        val uvsCount = mesh.mTextureCoords(0)?.remaining() ?: 0
        Debug.logi(TAG, "UVs count: $uvsCount")

        val uvs = FloatArray(uvsCount * 2) // U and V for every vertex

        if (uvsCount == mesh.mNumVertices()) {
            val uvsBuffer = mesh.mTextureCoords(0)
            for (i in 0 until uvsCount) {
                val uv = uvsBuffer!!.get()
                uvs[i] = uv.x()
                uvs[i] = uv.x()
            }
        } else {
            Debug.loge(TAG, "UVs count != Vertices count!")
        }

        return uvs
    }

    private fun processMaterials(materials: PointerBuffer) {
//        val materialsList = ArrayList<AIMaterial>()
//
//        for (x in 0 until count) {
//            materialsList.add(AIMaterial.create(materials.get(x)))
//        }
//
    }

}