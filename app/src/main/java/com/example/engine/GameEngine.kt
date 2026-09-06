package com.example.engine

import com.example.model.BlockShape
import com.example.model.GameState
import com.example.model.ShapeColors
import com.example.model.ShapeFactory
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

data class ScorePopupEvent(val score: Int, val timestamp: Long = System.currentTimeMillis())

data class ComboBlastEvent(
    val linesCleared: Int,
    val comboCount: Int,
    val scoreEarned: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class PreviewState(
    val cells: List<Pair<Int, Int>>,
    val clearedRows: List<Int>,
    val clearedCols: List<Int>
)

enum class SoundEvent {
    PLACE_BLOCK, CLEAR_LINE, COMBO, GAME_OVER
}

class GameEngine(
    private var randomGen: Random = Random.Default
) {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _lastComboEvent = MutableStateFlow<ComboBlastEvent?>(null)
    val lastComboEvent: StateFlow<ComboBlastEvent?> = _lastComboEvent.asStateFlow()

    private val _lastScorePopup = MutableStateFlow<ScorePopupEvent?>(null)
    val lastScorePopup: StateFlow<ScorePopupEvent?> = _lastScorePopup.asStateFlow()

    private val _soundEvents = MutableSharedFlow<SoundEvent>(extraBufferCapacity = 5)
    val soundEvents: SharedFlow<SoundEvent> = _soundEvents.asSharedFlow()

    private val _newHighScoreEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val newHighScoreEvent: SharedFlow<Unit> = _newHighScoreEvent.asSharedFlow()

    private var hasNotifiedHighScoreThisSession = false
    
    fun setRandomGenerator(random: Random) {
        randomGen = random
    }

    fun initGame(currentHighScore: Int, random: Random? = null) {
        if (random != null) {
            randomGen = random
        }
        var newGrid = Array(8) { IntArray(8) { 0 } }
        var newScore = 0

        // Pre-fill board with 4 random blocks to simulate mid-game start
        repeat(4) {
            val shape = ShapeFactory.getRandomShape(randomGen)
            val validSpots = mutableListOf<Pair<Int, Int>>()
            for (r in 0..7) {
                for (c in 0..7) {
                    if (canPlace(newGrid, shape, r, c)) {
                        validSpots.add(Pair(r, c))
                    }
                }
            }
            if (validSpots.isNotEmpty()) {
                val spot = validSpots.random(randomGen)
                val result = placeShapeSimulated(newGrid, shape, spot.first, spot.second)
                newGrid = result.first
                newScore += result.second
            }
        }

        val newDock = ShapeFactory.getRandomDockShapes(3, randomGen)
        hasNotifiedHighScoreThisSession = false

        _gameState.value = GameState(
            grid = newGrid,
            dock = newDock,
            score = newScore,
            highScore = maxOf(_gameState.value.highScore, currentHighScore),
            comboCount = 0,
            isGameOver = false,
            lastClearedIndices = emptyMap()
        )
        _lastComboEvent.value = null
    }

    fun updateHighScore(highScore: Int) {
        _gameState.value = _gameState.value.copy(highScore = maxOf(_gameState.value.highScore, highScore))
    }

    private fun placeShapeSimulated(
        currentGrid: Array<IntArray>,
        shape: BlockShape,
        targetRow: Int,
        targetCol: Int
    ): Pair<Array<IntArray>, Int> {
        val newGrid = Array(8) { r -> IntArray(8) { c -> currentGrid[r][c] } }
        val colorId = ShapeColors.getIdForColor(shape.color)

        for (r in 0 until shape.rows) {
            for (c in 0 until shape.cols) {
                if (shape.matrix[r][c] != 0) {
                    newGrid[targetRow + r][targetCol + c] = colorId
                }
            }
        }

        val fullRows = mutableListOf<Int>()
        for (r in 0 until 8) {
            if ((0 until 8).all { c -> newGrid[r][c] != 0 }) fullRows.add(r)
        }
        val fullCols = mutableListOf<Int>()
        for (c in 0 until 8) {
            if ((0 until 8).all { r -> newGrid[r][c] != 0 }) fullCols.add(c)
        }

        for (r in fullRows) {
            for (c in 0 until 8) newGrid[r][c] = 0
        }
        for (c in fullCols) {
            for (r in 0 until 8) newGrid[r][c] = 0
        }

        val tileCount = shape.tileCount
        val totalLines = fullRows.size + fullCols.size
        val lineBonus = if (totalLines > 0) 10 * totalLines * totalLines else 0
        val pointsEarned = tileCount + lineBonus

        return Pair(newGrid, pointsEarned)
    }

    fun canPlace(matrix: Array<IntArray>, shape: BlockShape, startRow: Int, startCol: Int): Boolean {
        for (r in 0 until shape.rows) {
            for (c in 0 until shape.cols) {
                if (shape.matrix[r][c] != 0) {
                    val targetR = startRow + r
                    val targetC = startCol + c

                    if (targetR !in 0..7 || targetC !in 0..7) return false
                    if (matrix[targetR][targetC] != 0) return false
                }
            }
        }
        return true
    }

    fun getPreviewState(shape: BlockShape, startRow: Int, startCol: Int): PreviewState? {
        val currentGrid = _gameState.value.grid
        if (!canPlace(currentGrid, shape, startRow, startCol)) return null

        val cells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until shape.rows) {
            for (c in 0 until shape.cols) {
                if (shape.matrix[r][c] != 0) {
                    cells.add(Pair(startRow + r, startCol + c))
                }
            }
        }

        val simGrid = _gameState.value.deepCopyGrid()
        for ((r, c) in cells) {
            simGrid[r][c] = 1
        }

        val clearedRows = mutableListOf<Int>()
        for (r in 0 until 8) {
            if ((0 until 8).all { c -> simGrid[r][c] != 0 }) clearedRows.add(r)
        }

        val clearedCols = mutableListOf<Int>()
        for (c in 0 until 8) {
            if ((0 until 8).all { r -> simGrid[r][c] != 0 }) clearedCols.add(c)
        }

        return PreviewState(cells, clearedRows, clearedCols)
    }

    fun placeShape(shapeIndex: Int, targetRow: Int, targetCol: Int): Boolean {
        val currentState = _gameState.value
        if (currentState.isGameOver) return false

        if (shapeIndex !in currentState.dock.indices) return false
        val shape = currentState.dock[shapeIndex] ?: return false

        val currentGrid = currentState.grid
        if (!canPlace(currentGrid, shape, targetRow, targetCol)) return false

        val newGrid = currentState.deepCopyGrid()
        val colorId = ShapeColors.getIdForColor(shape.color)

        for (r in 0 until shape.rows) {
            for (c in 0 until shape.cols) {
                if (shape.matrix[r][c] != 0) {
                    newGrid[targetRow + r][targetCol + c] = colorId
                }
            }
        }

        val updatedDock = currentState.dock.toMutableList()
        updatedDock[shapeIndex] = null

        val fullRows = mutableListOf<Int>()
        for (r in 0 until 8) {
            var isFull = true
            for (c in 0 until 8) {
                if (newGrid[r][c] == 0) {
                    isFull = false
                    break
                }
            }
            if (isFull) fullRows.add(r)
        }

        val fullCols = mutableListOf<Int>()
        for (c in 0 until 8) {
            var isFull = true
            for (r in 0 until 8) {
                if (newGrid[r][c] == 0) {
                    isFull = false
                    break
                }
            }
            if (isFull) fullCols.add(c)
        }

        val clearedIndices = mutableMapOf<Pair<Int, Int>, Int>()
        for (r in fullRows) {
            for (c in 0 until 8) clearedIndices[Pair(r, c)] = newGrid[r][c]
        }
        for (c in fullCols) {
            for (r in 0 until 8) clearedIndices[Pair(r, c)] = newGrid[r][c]
        }

        for ((r, c) in clearedIndices.keys) {
            newGrid[r][c] = 0
        }

        val tileCount = shape.tileCount * 5
        val totalLines = fullRows.size + fullCols.size
        val lineBonus = if (totalLines > 0) 50 * totalLines * totalLines else 0
        val comboBonus = if (totalLines > 0) currentState.comboCount * 40 else 0
        val pointsEarned = tileCount + lineBonus + comboBonus

        val newScore = currentState.score + pointsEarned
        val newComboCount = if (totalLines > 0) currentState.comboCount + 1 else 0
        _lastScorePopup.value = ScorePopupEvent(pointsEarned)

        val finalDock = if (updatedDock.all { it == null }) {
            ShapeFactory.getRandomDockShapes(3, randomGen)
        } else {
            updatedDock
        }

        var newHighScore = currentState.highScore
        if (newScore > currentState.highScore) {
            newHighScore = newScore
            if (!hasNotifiedHighScoreThisSession && currentState.highScore > 0) {
                hasNotifiedHighScoreThisSession = true
                _newHighScoreEvent.tryEmit(Unit)
            }
        }

        val isGameOver = checkGameOver(newGrid, finalDock)

        if (isGameOver) {
            _soundEvents.tryEmit(SoundEvent.GAME_OVER)
        } else if (totalLines > 1 || (totalLines > 0 && newComboCount > 1)) {
            _soundEvents.tryEmit(SoundEvent.COMBO)
        } else if (totalLines == 1) {
            _soundEvents.tryEmit(SoundEvent.CLEAR_LINE)
        } else {
            _soundEvents.tryEmit(SoundEvent.PLACE_BLOCK)
        }

        if (totalLines > 0) {
            _lastComboEvent.value = ComboBlastEvent(
                linesCleared = totalLines,
                comboCount = newComboCount,
                scoreEarned = pointsEarned
            )
        }

        _gameState.value = GameState(
            grid = newGrid,
            dock = finalDock,
            score = newScore,
            highScore = newHighScore,
            comboCount = newComboCount,
            isGameOver = isGameOver,
            lastClearedIndices = clearedIndices
        )

        return true
    }

    private fun checkGameOver(matrix: Array<IntArray>, dock: List<BlockShape?>): Boolean {
        val nonNullShapes = dock.filterNotNull()
        if (nonNullShapes.isEmpty()) return false

        for (shape in nonNullShapes) {
            for (r in 0 until 8) {
                for (c in 0 until 8) {
                    if (canPlace(matrix, shape, r, c)) return false
                }
            }
        }
        return true
    }
}
