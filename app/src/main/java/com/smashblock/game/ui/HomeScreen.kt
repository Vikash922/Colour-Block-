package com.smashblock.game.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.smashblock.game.ui.components.FloatingBlocks
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smashblock.game.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.smashblock.game.viewmodel.GameViewModel
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff

@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    onPlayClick: () -> Unit = {},
    onDailyChallengeClick: () -> Unit = {},
) {
    val gameState by viewModel.gameState.collectAsState()
    val highScore = gameState.highScore
    var showSettingsDialog by remember { mutableStateOf(false) }
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()
    val infiniteTransition = rememberInfiniteTransition(label = "Infinite")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "LogoFloat"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Solid deep blue background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A1F3D))
        )

        FloatingBlocks()

        // Settings Button
        IconButton(
            onClick = { showSettingsDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "Settings",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(36.dp)
            )
        }

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.graphicsLayer { translationY = floatOffset }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier.fillMaxWidth(0.9f),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Stats Panel
            ModernStatsGrid(highScore = highScore)

            // Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                JourneyButton(onClick = onDailyChallengeClick)
                ClassicButton(onClick = onPlayClick)
            }
        }

        // Settings Dialog
        if (showSettingsDialog) {
            Dialog(onDismissRequest = { showSettingsDialog = false }) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1A1F3D))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                        .padding(32.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Text(
                            text = "SETTINGS",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )

                        Button(
                            onClick = { viewModel.toggleSound() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSoundEnabled) Color(0xFF00C853) else Color(0xFFE53935)
                            ),
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = if (isSoundEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                                contentDescription = "Toggle Sound"
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                if (isSoundEnabled) "Sound: ON" else "Sound: OFF",
                                fontSize = 18.sp, fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { showSettingsDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Close", color = Color(0xFF1A1F3D), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernStatsGrid(highScore: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(0.95f),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCardBig(
            icon = Icons.Rounded.EmojiEvents,
            iconTint = Color(0xFFFFD54F),
            label = "HIGHSCORE",
            value = highScore.toString()
        )
    }
}

@Composable
fun StatCardBig(icon: ImageVector, iconTint: Color, label: String, value: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(24.dp))
            .background(Color(0xFF252B4D), RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0x33FFD700), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text(value, color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun JourneyButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "DailyScale")

    Box(
        modifier = Modifier
            .scale(scale)
            .fillMaxWidth(0.95f)
            .shadow(12.dp, RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFFFFB74D), Color(0xFFF57C00))), RoundedCornerShape(20.dp))
            .clickable(interactionSource = interactionSource, indication = LocalIndication.current, onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Journey",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun ClassicButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "PlayInfinite")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "PlayPulse"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.90f else 1f, label = "PlayScale")

    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(2500, delayMillis = 500), RepeatMode.Restart),
        label = "PlayShimmer"
    )

    Box(
        modifier = Modifier
            .scale(scale * pulse)
            .fillMaxWidth(0.95f)
            .shadow(20.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF00E676))
            .background(Brush.linearGradient(listOf(Color(0xFF38E06A), Color(0xFF0FB246))), RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .clickable(interactionSource = interactionSource, indication = LocalIndication.current, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Dashboard, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Classic",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                style = TextStyle(shadow = Shadow(color = Color.Black.copy(0.3f), offset = Offset(0f, 4f), blurRadius = 4f))
            )
        }

        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val xOffset = shimmerOffset * width
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.3f), Color.Transparent),
                    start = Offset(xOffset - 100f, 0f),
                    end = Offset(xOffset + 100f, height)
                )
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-16).dp, y = (0).dp)
                .background(Color.White, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text("Continue!", color = Color(0xFF0FB246), fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }
}
