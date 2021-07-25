package collision

data class BoundingPoints(
    var minX: Float = Float.POSITIVE_INFINITY,
    var maxX: Float = Float.NEGATIVE_INFINITY,
    var minY: Float = Float.POSITIVE_INFINITY,
    var maxY: Float = Float.NEGATIVE_INFINITY,
    var minZ: Float = Float.POSITIVE_INFINITY,
    var maxZ: Float = Float.NEGATIVE_INFINITY,
)