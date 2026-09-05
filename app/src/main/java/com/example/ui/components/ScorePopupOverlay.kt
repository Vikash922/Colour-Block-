package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.viewmodel.ScorePopupEvent
import kotlinx.coroutines.delay

@Composable
fun ScorePopupOverlay(scoreEvent: ScorePopupEvent?, modifier: Modifier = Modifier) {
    var isVisible by remember { mutableStateOf(false) }
    var currentScore by remember { mutableStateOf(0) }

    LaunchedEffect(scoreEvent) {
        if (scoreEvent != null) {
            currentScore = scoreEvent.score
            isVisible = false // reset
            delay(50)
            isVisible = true
            delay(800)
            isVisible = false
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(200)) + slideInVertically(tween(400)) { it / 2 },
            exit = fadeOut(tween(300))
        ) {
            Box {
                Text(
                    text = "+$currentScore",
                    color = Color.White,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    style = TextStyle.Default.copy(
                        drawStyle = Stroke(
                            miter = 10f,
                            width = 12f,
                            join = StrokeJoin.Round
                        )
                    )
                )
                Text(
                    text = "+$currentScore",
                    color = Color.White,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
