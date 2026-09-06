package com.smashblock.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import com.smashblock.game.engine.ScorePopupEvent
import kotlinx.coroutines.launch

@Composable
fun ScorePopupOverlay(scoreEvent: ScorePopupEvent?, modifier: Modifier = Modifier) {
    if (scoreEvent == null) return

    val floatY = remember(scoreEvent.timestamp) { Animatable(0f) }
    val alphaAnim = remember(scoreEvent.timestamp) { Animatable(1f) }
    val scaleAnim = remember(scoreEvent.timestamp) { Animatable(0.3f) }

    LaunchedEffect(scoreEvent.timestamp) {
        floatY.snapTo(0f)
        alphaAnim.snapTo(1f)
        scaleAnim.snapTo(0.3f)

        // Pop in
        launch {
            scaleAnim.animateTo(
                targetValue = 1.3f,
                animationSpec = tween(200, easing = FastOutSlowInEasing)
            )
            scaleAnim.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(100, easing = FastOutSlowInEasing)
            )
        }

        // Float upward
        launch {
            floatY.animateTo(
                targetValue = -200f,
                animationSpec = tween(1200, easing = LinearEasing)
            )
        }

        // Fade out near the end
        launch {
            kotlinx.coroutines.delay(600)
            alphaAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(600, easing = LinearEasing)
            )
        }
    }

    val scoreColor = when {
        scoreEvent.score >= 200 -> Color(0xFFFFD700) // Gold
        scoreEvent.score >= 100 -> Color(0xFF00E5FF) // Cyan
        else -> Color.White
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .offset { IntOffset(0, floatY.value.toInt()) }
                .alpha(alphaAnim.value)
                .scale(scaleAnim.value)
        ) {
            // Outline
            Text(
                text = "+${scoreEvent.score}",
                color = Color.Black.copy(alpha = 0.4f),
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                style = TextStyle.Default.copy(
                    drawStyle = Stroke(
                        miter = 10f,
                        width = 10f,
                        join = StrokeJoin.Round
                    )
                )
            )
            // Fill
            Text(
                text = "+${scoreEvent.score}",
                color = scoreColor,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
