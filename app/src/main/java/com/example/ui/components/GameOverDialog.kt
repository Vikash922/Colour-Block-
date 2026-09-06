package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.rounded.Refresh
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
fun GameOverDialog(
    score: Int,
    highScore: Int,
    onPlayAgain: () -> Unit,
    onHomeClick: () -> Unit = {}
) {
    var animationStarted by remember { mutableStateOf(false) }
    val isNewBest = score >= highScore && score > 0

    val scoreAnim by animateIntAsState(
        targetValue = if (animationStarted) score else 0,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "ScoreCount"
    )

    LaunchedEffect(Unit) {
        delay(400)
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
                .background(
                    if (isNewBest)
                        Brush.verticalGradient(listOf(Color(0xFF7B1FA2), Color(0xFF4A148C)))
                    else
                        Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF0D1442)))
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isNewBest) ConfettiBackground()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Title
                Text(
                    text = if (isNewBest) "NEW BEST!" else "GAME OVER",
                    color = if (isNewBest) Color(0xFFFFD54F) else Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Trophy
                if (isNewBest) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .shadow(24.dp, CircleShape, spotColor = Color(0xFFFFD700))
                            .background(
                                Brush.linearGradient(listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Trophy",
                            tint = Color.White,
                            modifier = Modifier.size(72.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Score Label
                Text(
                    text = "SCORE",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Animated Score
                Text(
                    text = scoreAnim.toString(),
                    color = Color.White,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Best Score
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Best: $highScore",
                        color = Color(0xFFFFD700),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Buttons Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home Button
                    val homeInteraction = remember { MutableInteractionSource() }
                    val homePressed by homeInteraction.collectIsPressedAsState()
                    val homeScale by animateFloatAsState(if (homePressed) 0.9f else 1f, label = "hs")

                    Box(
                        modifier = Modifier
                            .scale(homeScale)
                            .size(64.dp)
                            .shadow(12.dp, CircleShape)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                            .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                            .clip(CircleShape)
                            .clickable(interactionSource = homeInteraction, indication = null, onClick = onHomeClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Play Again Button
                    PlayAgainButton(onClick = onPlayAgain)
                }
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
            .size(80.dp)
            .shadow(20.dp, CircleShape, spotColor = Color(0xFF00E676))
            .background(
                Brush.linearGradient(listOf(Color(0xFF38E06A), Color(0xFF0FB246))),
                CircleShape
            )
            .clip(CircleShape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Refresh,
            contentDescription = "Play Again",
            tint = Color.White,
            modifier = Modifier.size(44.dp)
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
        val colors = listOf(
            Color(0xFFE91E63), Color(0xFF00BCD4), Color(0xFFFFEB3B),
            Color(0xFF4CAF50), Color(0xFFFF9800), Color(0xFF9C27B0)
        )
        for (i in 0 until 50) {
            val startX = (i * 37) % size.width
            val speed = 1f + (i % 3) * 0.5f
            val y = (offsetY * speed + i * 50) % size.height
            val color = colors[i % colors.size]
            val w = if (i % 3 == 0) 14f else 8f
            val h = if (i % 3 == 0) 8f else 14f
            drawRect(
                color = color.copy(alpha = 0.6f),
                topLeft = Offset(startX, y),
                size = androidx.compose.ui.geometry.Size(w, h)
            )
        }
    }
}
