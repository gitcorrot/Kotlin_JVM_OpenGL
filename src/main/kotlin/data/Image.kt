package data

import java.nio.ByteBuffer

data class Image(
    val data: ByteBuffer,
    val width: Int,
    val height: Int
)
