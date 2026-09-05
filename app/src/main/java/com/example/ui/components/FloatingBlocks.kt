package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin

@Composable
fun FloatingBlocks() {
    val infiniteTransition = rememberInfiniteTransition(label = "floatingBlocks")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Restart),
        label = "time"
    )

    val blocks = remember {
        List(8) {
            val startX = Math.random().toFloat()
            val speedY = (Math.random() * 0.5f + 0.5f).toFloat()
            val speedRot = (Math.random() * 2f - 1f).toFloat()
            val size = (Math.random() * 40f + 20f).toFloat()
            val color = listOf(
                Color(0xFFFF3B30), Color(0xFFFF9500), Color(0xFFFFCC00), 
                Color(0xFF4CD964), Color(0xFF5AC8FA), Color(0xFF5856D6)
            ).random().copy(alpha = 0.2f)
            BlockProps(startX, speedY, speedRot, size, color)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        blocks.forEachIndexed { index, block ->
            val phaseOffset = index * (Math.PI / 4).toFloat()
            val currentY = height * 1.2f - ((time * block.speedY * height / (2 * Math.PI.toFloat())) % (height * 1.5f))
            val currentX = block.startX * width + sin(time * 2f + phaseOffset) * 50f
            val rotation = time * block.speedRot * 180f / Math.PI.toFloat()
            
            rotate(degrees = rotation, pivot = Offset(currentX + block.size/2, currentY + block.size/2)) {
                drawRoundRect(
                    color = block.color,
                    topLeft = Offset(currentX, currentY),
                    size = Size(block.size, block.size),
                    cornerRadius = CornerRadius(block.size * 0.2f)
                )
            }
        }
    }
}

data class BlockProps(
    val startX: Float,
    val speedY: Float,
    val speedRot: Float,
    val size: Float,
    val color: Color
)
