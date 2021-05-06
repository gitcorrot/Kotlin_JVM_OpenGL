package data

data class Mesh(
    val vertices: ArrayList<Vertex>,
    var indices: IntArray?
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

        sb.append("Vertices size: ${vertices.size}\n")
        for (v in vertices) {
            sb.append("${v.position} | ${v.normal} | ${v.color} | ${v.textureCoordinates}\n")
        }

        indices?.let {
            sb.append("Indices size: ${it.size}\n")
            for (x in it.indices step 3) {
                sb.append("[${it[x]}, ${it[x+1]}, ${it[x+2]}]\n")
            }
        }

        return sb.toString()
    }
}