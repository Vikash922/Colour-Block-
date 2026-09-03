package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audio.SoundManager
import com.example.data.ScorePreferencesRepository
import com.example.ui.GameScreen
import com.example.ui.theme.ColorBlockTheme
import com.example.ui.theme.DeepSpace
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val soundManager by lazy { SoundManager(applicationContext) }
    private val scoreRepository by lazy { ScorePreferencesRepository(applicationContext) }

    private val gameViewModel: GameViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GameViewModel(scoreRepository, soundManager) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ColorBlockTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DeepSpace
                ) {
                    GameScreen(viewModel = gameViewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
