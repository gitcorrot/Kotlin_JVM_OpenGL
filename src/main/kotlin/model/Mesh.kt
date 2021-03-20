package model

data class Mesh(
    val vertices: ArrayList<Vertex>,
    val indices: IntArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Mesh

        if (vertices != other.vertices) return false
        if (!indices.contentEquals(other.indices)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = vertices.hashCode()
        result = 31 * result + indices.contentHashCode()
        return result
    }
}