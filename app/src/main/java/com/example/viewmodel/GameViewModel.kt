package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.ScorePreferencesRepository
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

class GameViewModel(
    private val scoreRepository: ScorePreferencesRepository,
    private val soundManager: SoundManager? = null
) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _lastComboEvent = MutableStateFlow<ComboBlastEvent?>(null)
    val lastComboEvent: StateFlow<ComboBlastEvent?> = _lastComboEvent.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    private val _newHighScoreEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val newHighScoreEvent: SharedFlow<Unit> = _newHighScoreEvent.asSharedFlow()

    private var hasNotifiedHighScoreThisSession = false

    init {
        viewModelScope.launch {
            val savedHighScore = scoreRepository.highScoreFlow.first()
            _gameState.update { it.copy(highScore = savedHighScore) }
            initGame()
        }
    }

    fun toggleSound() {
        _isSoundEnabled.update { enabled ->
            val next = !enabled
            soundManager?.isMuted = !next
            next
        }
    }

    /**
     * Resets the 8x8 matrix, loads high score from DataStore, resets score/combo,
     * and populates the 3-piece dock using ShapeFactory. Also pre-fills board.
     */
    fun initGame() {
        viewModelScope.launch {
            val currentHighScore = scoreRepository.highScoreFlow.first()
            var newGrid = Array(8) { IntArray(8) { 0 } }
            var newScore = 0

            // Pre-fill board with 4 random blocks to simulate mid-game start
            repeat(4) {
                val shape = ShapeFactory.getRandomShape()
                val validSpots = mutableListOf<Pair<Int, Int>>()
                for (r in 0..7) {
                    for (c in 0..7) {
                        if (canPlace(newGrid, shape, r, c)) {
                            validSpots.add(Pair(r, c))
                        }
                    }
                }
                if (validSpots.isNotEmpty()) {
                    val spot = validSpots.random()
                    val result = placeShapeSimulated(newGrid, shape, spot.first, spot.second)
                    newGrid = result.first
                    newScore += result.second
                }
            }

            val newDock = ShapeFactory.getRandomDockShapes(3)
            hasNotifiedHighScoreThisSession = false

            _gameState.value = GameState(
                grid = newGrid,
                dock = newDock,
                score = newScore,
                highScore = maxOf(_gameState.value.highScore, currentHighScore),
                comboCount = 0,
                isGameOver = false,
                lastClearedIndices = emptySet()
            )
            _lastComboEvent.value = null
        }
    }

    /**
     * Helper to simulate placing a shape and calculating clears without updating full state
     */
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

    /**
     * Validates boundary limits (8x8) and ensures shape matrix 1s only overlap 0s in the grid.
     */
    fun canPlace(matrix: Array<IntArray>, shape: BlockShape, startRow: Int, startCol: Int): Boolean {
        for (r in 0 until shape.rows) {
            for (c in 0 until shape.cols) {
                if (shape.matrix[r][c] != 0) {
                    val targetR = startRow + r
                    val targetC = startCol + c

                    // Check bounds
                    if (targetR !in 0..7 || targetC !in 0..7) {
                        return false
                    }

                    // Check overlap
                    if (matrix[targetR][targetC] != 0) {
                        return false
                    }
                }
            }
        }
        return true
    }

    /**
     * Returns the preview state including cells that would be occupied and rows/cols that would be cleared.
     */
    fun getPreviewState(shape: BlockShape, startRow: Int, startCol: Int): PreviewState? {
        val currentGrid = _gameState.value.grid
        if (!canPlace(currentGrid, shape, startRow, startCol)) {
            return null
        }

        val cells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until shape.rows) {
            for (c in 0 until shape.cols) {
                if (shape.matrix[r][c] != 0) {
                    cells.add(Pair(startRow + r, startCol + c))
                }
            }
        }

        // Simulate placement to find cleared rows/cols
        val simGrid = _gameState.value.deepCopyGrid()
        for ((r, c) in cells) {
            simGrid[r][c] = 1
        }

        val clearedRows = mutableListOf<Int>()
        for (r in 0 until 8) {
            if ((0 until 8).all { c -> simGrid[r][c] != 0 }) {
                clearedRows.add(r)
            }
        }

        val clearedCols = mutableListOf<Int>()
        for (c in 0 until 8) {
            if ((0 until 8).all { r -> simGrid[r][c] != 0 }) {
                clearedCols.add(c)
            }
        }

        return PreviewState(cells, clearedRows, clearedCols)
    }

    /**
     * Places the piece on the grid, evaluates line clears, calculates score and combo,
     * updates dock and high score, and checks for game over.
     */
    fun placeShape(shapeIndex: Int, targetRow: Int, targetCol: Int): Boolean {
        val currentState = _gameState.value
        if (currentState.isGameOver) return false

        if (shapeIndex !in currentState.dock.indices) return false
        val shape = currentState.dock[shapeIndex] ?: return false

        val currentGrid = currentState.grid
        if (!canPlace(currentGrid, shape, targetRow, targetCol)) {
            return false
        }

        // 1. Copy grid and stamp shape matrix
        val newGrid = currentState.deepCopyGrid()
        val colorId = ShapeColors.getIdForColor(shape.color)

        for (r in 0 until shape.rows) {
            for (c in 0 until shape.cols) {
                if (shape.matrix[r][c] != 0) {
                    newGrid[targetRow + r][targetCol + c] = colorId
                }
            }
        }

        // 2. Clear shape from dock index
        val updatedDock = currentState.dock.toMutableList()
        updatedDock[shapeIndex] = null

        // 3. Evaluate all 8 rows and 8 columns simultaneously
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

        val clearedIndices = mutableSetOf<Pair<Int, Int>>()
        for (r in fullRows) {
            for (c in 0 until 8) {
                clearedIndices.add(Pair(r, c))
            }
        }
        for (c in fullCols) {
            for (r in 0 until 8) {
                clearedIndices.add(Pair(r, c))
            }
        }

        // 4. Reset intersecting full cells to 0
        for ((r, c) in clearedIndices) {
            newGrid[r][c] = 0
        }

        // 5. Score calculation
        val tileCount = shape.tileCount
        val totalLines = fullRows.size + fullCols.size
        val lineBonus = if (totalLines > 0) 10 * totalLines * totalLines else 0
        val comboBonus = if (totalLines > 0) currentState.comboCount * 15 else 0
        val pointsEarned = tileCount + lineBonus + comboBonus

        val newScore = currentState.score + pointsEarned
        val newComboCount = if (totalLines > 0) currentState.comboCount + 1 else 0

        // 6. Refill dock if all 3 placed
        val finalDock = if (updatedDock.all { it == null }) {
            ShapeFactory.getRandomDockShapes(3)
        } else {
            updatedDock
        }

        // 7. Update High Score and notify event
        var newHighScore = currentState.highScore
        if (newScore > currentState.highScore) {
            newHighScore = newScore
            if (!hasNotifiedHighScoreThisSession && currentState.highScore > 0) { // Don't notify if previous high score was 0
                hasNotifiedHighScoreThisSession = true
                _newHighScoreEvent.tryEmit(Unit)
            }
            viewModelScope.launch {
                scoreRepository.saveHighScore(newHighScore)
            }
        }

        // 8. Check Game Over
        val isGameOver = checkGameOver(newGrid, finalDock)

        // 9. Sound and event triggers
        if (isGameOver) {
            soundManager?.playGameOver()
        } else if (totalLines > 1 || (totalLines > 0 && newComboCount > 1)) {
            soundManager?.playCombo(newComboCount)
        } else if (totalLines == 1) {
            soundManager?.playClearLine()
        } else {
            soundManager?.playPlaceBlock()
        }

        if (totalLines > 0) {
            _lastComboEvent.value = ComboBlastEvent(
                linesCleared = totalLines,
                comboCount = newComboCount,
                scoreEarned = pointsEarned
            )
        }

        // 10. Update state
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

    /**
     * Iterates all non-null shapes in the dock across every row (0..7) and column (0..7).
     * If no shape fits anywhere, returns true (game over).
     */
    fun checkGameOver(matrix: Array<IntArray>, dock: List<BlockShape?>): Boolean {
        val nonNullShapes = dock.filterNotNull()
        if (nonNullShapes.isEmpty()) return false

        for (shape in nonNullShapes) {
            for (r in 0 until 8) {
                for (c in 0 until 8) {
                    if (canPlace(matrix, shape, r, c)) {
                        return false // At least one piece can still be placed
                    }
                }
            }
        }
        return true
    }

    override fun onCleared() {
        super.onCleared()
        soundManager?.release()
    }
}
