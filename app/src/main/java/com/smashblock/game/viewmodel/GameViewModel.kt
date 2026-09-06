package com.smashblock.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smashblock.game.audio.SoundManager
import com.smashblock.game.data.ScorePreferencesRepository
import com.smashblock.game.model.BlockShape
import com.smashblock.game.model.GameState
import com.smashblock.game.engine.GameEngine
import com.smashblock.game.engine.ComboBlastEvent
import com.smashblock.game.engine.ScorePopupEvent
import com.smashblock.game.engine.PreviewState
import com.smashblock.game.engine.SoundEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(
    private val scoreRepository: ScorePreferencesRepository,
    private val soundManager: SoundManager? = null
) : ViewModel() {

    private val engine = GameEngine()

    val gameState: StateFlow<GameState> = engine.gameState
    val lastComboEvent: StateFlow<ComboBlastEvent?> = engine.lastComboEvent
    val lastScorePopup: StateFlow<ScorePopupEvent?> = engine.lastScorePopup
    val newHighScoreEvent: SharedFlow<Unit> = engine.newHighScoreEvent

    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    private val _isDailyChallenge = MutableStateFlow(false)
    val isDailyChallenge: StateFlow<Boolean> = _isDailyChallenge.asStateFlow()

    init {
        viewModelScope.launch {
            val savedHighScore = scoreRepository.highScoreFlow.first()
            engine.initGame(savedHighScore)
            
            // Listen for high score changes to save them
            launch {
                gameState.collect { state ->
                    if (state.highScore > savedHighScore) {
                        scoreRepository.saveHighScore(state.highScore)
                    }
                }
            }
            
            // Listen for sound events
            launch {
                engine.soundEvents.collect { event ->
                    when (event) {
                        SoundEvent.PLACE_BLOCK -> soundManager?.playPlaceBlock()
                        SoundEvent.CLEAR_LINE -> soundManager?.playClearLine()
                        SoundEvent.COMBO -> soundManager?.playCombo(gameState.value.comboCount)
                        SoundEvent.GAME_OVER -> soundManager?.playGameOver()
                    }
                }
            }
        }
    }

    fun startDailyChallenge() {
        val todaySeed = java.time.LocalDate.now().toEpochDay()
        _isDailyChallenge.value = true
        viewModelScope.launch {
            val savedHighScore = scoreRepository.highScoreFlow.first()
            engine.initGame(savedHighScore, Random(todaySeed))
        }
    }

    fun startRegularGame() {
        _isDailyChallenge.value = false
        viewModelScope.launch {
            val savedHighScore = scoreRepository.highScoreFlow.first()
            engine.initGame(savedHighScore, Random.Default)
        }
    }

    fun toggleSound() {
        _isSoundEnabled.update { enabled ->
            val next = !enabled
            soundManager?.isMuted = !next
            next
        }
    }

    fun initGame() {
        if (_isDailyChallenge.value) startDailyChallenge() else startRegularGame()
    }

    fun getPreviewState(shape: BlockShape, startRow: Int, startCol: Int): PreviewState? {
        return engine.getPreviewState(shape, startRow, startCol)
    }

    fun placeShape(shapeIndex: Int, targetRow: Int, targetCol: Int): Boolean {
        return engine.placeShape(shapeIndex, targetRow, targetCol)
    }

    override fun onCleared() {
        super.onCleared()
        soundManager?.release()
    }
}
