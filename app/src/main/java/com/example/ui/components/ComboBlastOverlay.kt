package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.ComboBlastEvent
import com.example.ui.theme.*

@Composable
fun ComboBlastOverlay(
    comboEvent: ComboBlastEvent?,
    modifier: Modifier = Modifier
) {
    if (comboEvent == null) return

    val floatAnim = remember(comboEvent.timestamp) { Animatable(0f) }
    val alphaAnim = remember(comboEvent.timestamp) { Animatable(1f) }
    val scaleAnim = remember(comboEvent.timestamp) { Animatable(0.5f) }

    LaunchedEffect(comboEvent.timestamp) {
        floatAnim.snapTo(0f)
        alphaAnim.snapTo(1f)
        scaleAnim.snapTo(0.5f)
        
        scaleAnim.animateTo(
            targetValue = 1.2f,
            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
        )
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
        )

        // Float up and fade out over 1000ms
        floatAnim.animateTo(
            targetValue = -120f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
        alphaAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        )
    }

    val bannerText = when {
        comboEvent.comboCount > 2 -> "Combo ${comboEvent.comboCount}"
        comboEvent.comboCount == 2 -> "Combo 2"
        comboEvent.linesCleared >= 3 -> "Awesome!"
        comboEvent.linesCleared == 2 -> "Great!"
        else -> "Good!"
    }

    val textColor = when {
        comboEvent.comboCount >= 2 -> ElectricGold
        comboEvent.linesCleared >= 2 -> HotPink
        else -> NeonCyan
    }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset { IntOffset(0, floatAnim.value.toInt()) }
                .alpha(alphaAnim.value)
                .scale(scaleAnim.value)
        ) {
            // Outline Text
            Text(
                text = bannerText,
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                style = TextStyle.Default.copy(
                    drawStyle = Stroke(
                        miter = 10f,
                        width = 8f,
                        join = StrokeJoin.Round
                    )
                )
            )
            // Fill Text
            Text(
                text = bannerText,
                color = textColor,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
