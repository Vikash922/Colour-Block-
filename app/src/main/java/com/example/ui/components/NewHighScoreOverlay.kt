package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun NewHighScoreOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(tween(600, easing = ElasticOutEasing)) + fadeIn(tween(400)),
        exit = scaleOut(tween(300, easing = FastOutLinearInEasing)) + fadeOut(tween(300)),
        modifier = modifier
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "rays")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
            label = "ray_rotation"
        )
        
        val scalePulse by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "pulse"
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Rotating Sunburst Rays
            Canvas(modifier = Modifier.size(300.dp).rotate(rotation)) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width / 2
                val numRays = 12
                val angleStep = 360f / numRays
                
                for (i in 0 until numRays) {
                    drawArc(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFD700).copy(alpha = 0.4f), Color.Transparent),
                            center = center,
                            radius = radius
                        ),
                        startAngle = i * angleStep,
                        sweepAngle = angleStep / 2,
                        useCenter = true
                    )
                }
            }

            // Central Badge
            Box(
                modifier = Modifier
                    .scale(scalePulse)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Brush.linearGradient(
                        colors = listOf(Color(0xFFFFB74D), Color(0xFFFF9800), Color(0xFFF57C00))
                    ))
                    .padding(4.dp) // border thickness
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF1E1E2E))
                    .padding(horizontal = 40.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = "Trophy",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "NEW BEST!",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Amazing Job",
                        color = Color(0xFFFFD700),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Reusable ElasticOut easing since compose doesn't have it built-in exactly
private val ElasticOutEasing = Easing { fraction ->
    val c4 = (2f * Math.PI) / 3f
    when (fraction) {
        0f -> 0f
        1f -> 1f
        else -> Math.pow(2.0, -10.0 * fraction).toFloat() * Math.sin((fraction * 10f - 0.75f) * c4).toFloat() + 1f
    }
}
