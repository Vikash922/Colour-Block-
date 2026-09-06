package com.example.model

data class GameState(
    val grid: Array<IntArray> = Array(8) { IntArray(8) { 0 } },
    val dock: List<BlockShape?> = listOf(null, null, null),
    val score: Int = 0,
    val highScore: Int = 0,
    val comboCount: Int = 0,
    val isGameOver: Boolean = false,
    val lastClearedIndices: Set<Pair<Int, Int>> = emptySet()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

        if (score != other.score) return false
        if (highScore != other.highScore) return false
        if (comboCount != other.comboCount) return false
        if (isGameOver != other.isGameOver) return false
        if (lastClearedIndices != other.lastClearedIndices) return false
        if (dock != other.dock) return false
        if (!grid.contentDeepEquals(other.grid)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = score
        result = 31 * result + highScore
        result = 31 * result + comboCount
        result = 31 * result + isGameOver.hashCode()
        result = 31 * result + lastClearedIndices.hashCode()
        result = 31 * result + dock.hashCode()
        result = 31 * result + grid.contentDeepHashCode()
        return result
    }

    fun deepCopyGrid(): Array<IntArray> {
        return Array(grid.size) { r -> grid[r].clone() }
    }
}
