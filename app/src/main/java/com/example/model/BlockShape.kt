package com.example.model

import androidx.compose.ui.graphics.Color

data class BlockShape(
    val id: String,
    val matrix: Array<IntArray>,
    val color: Color
) {
    val rows: Int get() = matrix.size
    val cols: Int get() = if (matrix.isNotEmpty()) matrix[0].size else 0

    val tileCount: Int by lazy {
        matrix.sumOf { row -> row.count { it != 0 } }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BlockShape

        if (id != other.id) return false
        if (color != other.color) return false
        if (!matrix.contentDeepEquals(other.matrix)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + color.hashCode()
        result = 31 * result + matrix.contentDeepHashCode()
        return result
    }
}
