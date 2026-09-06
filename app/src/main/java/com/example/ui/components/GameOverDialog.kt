package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

@Composable
fun GameOverDialog(score: Int, highScore: Int, onPlayAgain: () -> Unit) {
    var animationStarted by remember { mutableStateOf(false) }
    
    val scoreAnim by animateIntAsState(
        targetValue = if (animationStarted) score else 0,
        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
        label = "ScoreCount"
    )

    LaunchedEffect(Unit) {
        delay(300)
        animationStarted = true
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF7B1FA2), Color(0xFF4A148C)))),
            contentAlignment = Alignment.Center
        ) {
            ConfettiBackground()

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Best Score!",
                    color = Color(0xFFFFD54F),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Big Trophy
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .shadow(24.dp, RoundedCornerShape(80.dp), spotColor = Color(0xFFFFD700))
                        .background(
                            Brush.linearGradient(listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))),
                            RoundedCornerShape(80.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Trophy",
                        tint = Color.White,
                        modifier = Modifier.size(90.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Score",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = scoreAnim.toString(),
                    color = Color.White,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(64.dp))

                // Custom Play Button
                PlayAgainButton(onClick = onPlayAgain)
            }
        }
    }
}

@Composable
fun PlayAgainButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.90f else 1f, label = "PlayScale")
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "PlayPulse"
    )

    Box(
        modifier = Modifier
            .scale(scale * pulse)
            .width(200.dp)
            .height(72.dp)
            .shadow(20.dp, RoundedCornerShape(36.dp), spotColor = Color(0xFFFFB74D))
            .background(Brush.horizontalGradient(listOf(Color(0xFFFFB74D), Color(0xFFF57C00))), RoundedCornerShape(36.dp))
            .clip(RoundedCornerShape(36.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.PlayArrow,
            contentDescription = "Play Again",
            tint = Color.White,
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
fun ConfettiBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Restart),
        label = "confetti_fall"
    )

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val colors = listOf(Color(0xFFE91E63), Color(0xFF00BCD4), Color(0xFFFFEB3B), Color(0xFF4CAF50))
        for (i in 0 until 40) {
            val startX = (i * 37) % size.width
            val speed = 1f + (i % 3) * 0.5f
            val y = (offsetY * speed + i * 50) % size.height
            val color = colors[i % colors.size]
            drawRect(
                color = color,
                topLeft = Offset(startX, y),
                size = androidx.compose.ui.geometry.Size(16f, 16f)
            )
        }
    }
}
