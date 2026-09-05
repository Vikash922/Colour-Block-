package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audio.SoundManager
import com.example.data.ScorePreferencesRepository
import com.example.ui.GameScreen
import com.example.ui.theme.ColorBlockTheme
import com.example.ui.theme.DeepSpace
import com.example.updater.AppUpdater
import com.example.viewmodel.GameViewModel

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.HomeScreen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

import kotlinx.coroutines.delay

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
                    AppUpdater("https://raw.githubusercontent.com/Vikash922/ColorBlock/main/update.json")
                    
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "splash") {
                        composable("splash") {
                            SplashScreen {
                                navController.navigate("home") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        }
                        composable("home") {
                            HomeScreen(
                                viewModel = gameViewModel,
                                onPlayClick = {
                                    gameViewModel.startRegularGame()
                                    navController.navigate("game")
                                },
                                onDailyChallengeClick = {
                                    gameViewModel.startDailyChallenge()
                                    navController.navigate("game")
                                }
                            )
                        }
                        composable("game") {
                            GameScreen(
                                viewModel = gameViewModel,
                                onHomeClick = { 
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val scale = remember { Animatable(0.5f) }
    
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = androidx.compose.animation.core.FastOutSlowInEasing)
        )
        delay(800) // Hold for a bit
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2C39B0), // Deep blue top
                        Color(0xFF568FEB)  // Lighter blue bottom
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.scale(scale.value)) {
            androidx.compose.foundation.Image(painter = androidx.compose.ui.res.painterResource(id = R.drawable.logo), contentDescription = "Logo", modifier = Modifier.fillMaxWidth(0.8f), contentScale = androidx.compose.ui.layout.ContentScale.Fit)
        }
    }
}
