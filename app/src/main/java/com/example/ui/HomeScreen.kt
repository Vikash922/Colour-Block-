package com.example.ui

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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun HomeScreen(
    onPlayClick: () -> Unit = {},
    onDailyChallengeClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    var touchEffects by remember { mutableStateOf(listOf<Offset>()) }
    val infiniteTransition = rememberInfiniteTransition(label = "Infinite")
    val scope = rememberCoroutineScope()
    
    // Smooth floating animation for the logo
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "LogoFloat"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.pressed }
                        if (change != null) {
                            val position = change.position
                            touchEffects = touchEffects + position
                            scope.launch {
                                delay(650)
                                touchEffects = touchEffects - position
                            }
                        }
                    }
                }
            }
    ) {
        // 1. Dynamic Background
        AnimatedMeshBackground()

        // 2. Touch Effects overlay
        touchEffects.forEach { offset ->
            key(offset) {
                TapRippleEffect(offset = offset)
            }
        }

        // 3. Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            // --- TOP: LOGO & SETTINGS ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .graphicsLayer { translationY = floatOffset }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Same Blast Logo",
                        modifier = Modifier.fillMaxWidth(0.9f),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // --- MIDDLE: STATS PANEL ---
            ModernStatsGrid()

            // --- BOTTOM: MAIN BUTTONS ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DailyChallengeButton(onClick = onDailyChallengeClick)
                PlayButton(onClick = onPlayClick)
            }
        }
    }
}

@Composable
fun ModernStatsGrid() {
    Column(
        modifier = Modifier.fillMaxWidth(0.95f),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top: Huge Score Card
        StatCardBig(
            icon = Icons.Rounded.EmojiEvents, 
            iconTint = Color(0xFFFFD54F), 
            label = "HIGHSCORE", 
            value = "6,556"
        )
        
        // Bottom: Streak and Levels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCardSmall(
                modifier = Modifier.weight(1f), 
                icon = Icons.Rounded.LocalFireDepartment, 
                iconTint = Color(0xFFFF5252), 
                label = "STREAK", 
                value = "12"
            )
            StatCardSmall(
                modifier = Modifier.weight(1f), 
                icon = Icons.Rounded.Star, 
                iconTint = Color(0xFF64B5F6), 
                label = "LEVEL", 
                value = "42"
            )
        }
    }
}

@Composable
fun StatCardBig(icon: ImageVector, iconTint: Color, label: String, value: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(24.dp))
            .background(Color(0x33FFFFFF), RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(listOf(Color.White.copy(0.1f), Color.Transparent)))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0x44000000), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text(value, color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun StatCardSmall(modifier: Modifier = Modifier, icon: ImageVector, iconTint: Color, label: String, value: String) {
    Box(
        modifier = modifier
            .shadow(12.dp, RoundedCornerShape(20.dp))
            .background(Color(0x33FFFFFF), RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(Color.White.copy(0.1f), Color.Transparent)))
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun DailyChallengeButton(onClick: () -> Unit) {
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
            Icon(Icons.Rounded.Event, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "DAILY CHALLENGE",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }
        
        // New Badge
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 10.dp, y = (-8).dp)
                .background(Color(0xFFE53935), RoundedCornerShape(10.dp))
                .border(2.dp, Color.White, RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text("NEW!", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun PlayButton(onClick: () -> Unit) {
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
    
    // Shimmer effect calculation
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
            .background(Brush.linearGradient(listOf(Color(0xFF00E676), Color(0xFF00B0FF))), RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .clickable(interactionSource = interactionSource, indication = LocalIndication.current, onClick = onClick)
    ) {
        // Button Content
        Row(
            modifier = Modifier.padding(vertical = 20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "PLAY NOW",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                style = TextStyle(shadow = Shadow(color = Color.Black.copy(0.3f), offset = Offset(0f, 4f), blurRadius = 4f))
            )
        }

        // Shimmer shine
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val xOffset = shimmerOffset * width
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.4f), Color.Transparent),
                    start = Offset(xOffset - 100f, 0f),
                    end = Offset(xOffset + 100f, height)
                )
            )
        }
    }
}

@Composable
fun TapRippleEffect(offset: Offset) {
    val radius = remember { Animatable(0f) }
    val alpha = remember { Animatable(0.8f) }
    
    LaunchedEffect(Unit) {
        launch {
            radius.animateTo(350f, animationSpec = tween(600, easing = FastOutSlowInEasing))
        }
        launch {
            alpha.animateTo(0f, animationSpec = tween(600, easing = LinearEasing))
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Color.White.copy(alpha = alpha.value * 0.4f),
            radius = radius.value,
            center = offset
        )
        drawCircle(
            color = Color(0xFF64B5F6).copy(alpha = alpha.value),
            radius = radius.value * 0.8f,
            center = offset,
            style = Stroke(width = 16f)
        )
        drawCircle(
            color = Color.White.copy(alpha = alpha.value * 0.8f),
            radius = radius.value * 0.2f,
            center = offset
        )
    }
}

@Composable
fun AnimatedMeshBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart),
        label = "time"
    )

    Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A))) { // Dark slate background
        val width = size.width
        val height = size.height

        // Orb 1: Deep Blue
        val x1 = width * 0.5f + cos(time) * width * 0.3f
        val y1 = height * 0.3f + sin(time * 0.8f) * height * 0.2f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF3B82F6).copy(alpha = 0.4f), Color.Transparent),
                center = Offset(x1, y1),
                radius = width * 0.8f
            ),
            center = Offset(x1, y1),
            radius = width * 0.8f
        )

        // Orb 2: Purple
        val x2 = width * 0.2f + sin(time * 1.2f) * width * 0.4f
        val y2 = height * 0.7f + cos(time * 1.1f) * height * 0.3f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.4f), Color.Transparent),
                center = Offset(x2, y2),
                radius = width * 0.9f
            ),
            center = Offset(x2, y2),
            radius = width * 0.9f
        )
        
        // Orb 3: Cyan
        val x3 = width * 0.8f + cos(time * 0.9f) * width * 0.3f
        val y3 = height * 0.8f + sin(time * 1.3f) * height * 0.2f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF06B6D4).copy(alpha = 0.4f), Color.Transparent),
                center = Offset(x3, y3),
                radius = width * 0.7f
            ),
            center = Offset(x3, y3),
            radius = width * 0.7f
        )
        
        // Background Grid Pattern (subtle)
        val squareSize = 140f
        val spacing = 280f
        val yOffset = (time * 50f) % spacing
        for (i in -4..20) {
            for (j in -2..10) {
                val gx = j * spacing + (i % 2) * (spacing / 2)
                val gy = i * spacing + yOffset
                drawRect(
                    color = Color.White.copy(alpha = 0.03f),
                    topLeft = Offset(gx, gy),
                    size = androidx.compose.ui.geometry.Size(squareSize, squareSize)
                )
            }
        }
    }
}
