package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricGold
import com.example.ui.theme.HotPink
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SunsetOrange
import com.example.viewmodel.ComboBlastEvent

@Composable
fun ComboBlastOverlay(
    comboEvent: ComboBlastEvent?,
    modifier: Modifier = Modifier
) {
    if (comboEvent == null) return

    val floatAnim = remember(comboEvent.timestamp) { Animatable(0f) }
    val alphaAnim = remember(comboEvent.timestamp) { Animatable(1f) }

    LaunchedEffect(comboEvent.timestamp) {
        floatAnim.snapTo(0f)
        alphaAnim.snapTo(1f)
        // Float up and fade out over 900ms
        floatAnim.animateTo(
            targetValue = -70f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
        alphaAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        )
    }

    val bannerText = when {
        comboEvent.comboCount > 2 -> "COMBO x${comboEvent.comboCount} BLAST!"
        comboEvent.comboCount == 2 -> "COMBO x2 BLAST!"
        comboEvent.linesCleared >= 3 -> "TRIPLE BLAST!"
        comboEvent.linesCleared == 2 -> "DOUBLE BLAST!"
        else -> "LINE CLEAR!"
    }

    val gradientColors = when {
        comboEvent.comboCount >= 2 -> listOf(HotPink, SunsetOrange, ElectricGold)
        comboEvent.linesCleared >= 2 -> listOf(NeonCyan, HotPink)
        else -> listOf(NeonCyan, Color(0xFF38BDF8))
    }

    Box(modifier = modifier.fillMaxSize()) {
        ParticleExplosion(
            progress = 1f - alphaAnim.value,
            colors = gradientColors,
            modifier = Modifier.fillMaxSize()
        )
        
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset { IntOffset(0, floatAnim.value.toInt()) }
                .alpha(alphaAnim.value)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.horizontalGradient(gradientColors))
                    .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (comboEvent.comboCount >= 2 || comboEvent.linesCleared >= 2) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = bannerText,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "+${comboEvent.scoreEarned} PTS",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ParticleExplosion(progress: Float, colors: List<Color>, modifier: Modifier = Modifier) {
    val particles = remember {
        List(30) {
            val angle = (Math.random() * 2 * Math.PI).toFloat()
            val speed = (Math.random() * 300 + 100).toFloat()
            Triple(
                Math.cos(angle.toDouble()).toFloat() * speed,
                Math.sin(angle.toDouble()).toFloat() * speed,
                colors.random()
            )
        }
    }
    
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
        particles.forEach { (vx, vy, color) ->
            val alpha = (1f - progress).coerceIn(0f, 1f)
            val currentX = center.x + vx * progress
            val currentY = center.y + vy * progress
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = 12.dp.toPx() * (1f - progress * 0.7f),
                center = androidx.compose.ui.geometry.Offset(currentX, currentY)
            )
        }
    }
}
