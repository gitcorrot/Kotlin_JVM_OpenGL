package data

data class Mesh(
    val vertices: ArrayList<Vertex>,
    val indices: IntArray?
) {

    constructor(vertices: ArrayList<Vertex>) : this(vertices, null)

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

    override fun toString(): String {
        val sb = StringBuilder()

        for (v in vertices) {
            sb.append("${v.position} | ${v.normal} | ${v.textureCoordinates}\n")
        }

        indices?.let {
            for (i in it) {
                sb.append("$i\n")
            }
        }

        return sb.toString()
    }
}