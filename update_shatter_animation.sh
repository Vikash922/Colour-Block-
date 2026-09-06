#!/bin/bash
cat << 'INNER_EOF' > app/src/main/java/com/example/ui/components/BoardComposable.kt
package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import com.example.model.ShapeColors
import com.example.ui.theme.GridBackground
import com.example.ui.theme.GridLine
import com.example.engine.PreviewState

@Composable
fun BoardComposable(
    grid: Array<IntArray>,
    previewState: PreviewState?,
    ghostColor: Color?,
    lastClearedIndices: Map<Pair<Int, Int>, Int>,
    onBoardGloballyPositioned: (LayoutCoordinates) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "ghostPulse")
    val ghostPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(800, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val clearAnim = remember(lastClearedIndices) { Animatable(1f) }

    LaunchedEffect(lastClearedIndices) {
        if (lastClearedIndices.isNotEmpty()) {
            clearAnim.snapTo(1f)
            clearAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 600, easing = LinearEasing)
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8f))
            .background(GridLine)
            .onGloballyPositioned { coordinates ->
                onBoardGloballyPositioned(coordinates)
            }
            .testTag("game_board")
    ) {
        val clearAlpha = clearAnim.value

        Canvas(modifier = Modifier.fillMaxSize()) {
            val boardWidth = size.width
            val boardHeight = size.height
            val spacing = 2f
            val totalSpacing = spacing * 7
            val cellSize = (boardWidth - totalSpacing) / 8f
            val cornerRadius = CornerRadius(cellSize * 0.15f, cellSize * 0.15f)

            // 1. Draw Empty Grid Cell Slots
            for (r in 0 until 8) {
                for (c in 0 until 8) {
                    val x = c * (cellSize + spacing)
                    val y = r * (cellSize + spacing)

                    drawRoundRect(
                        color = GridBackground,
                        topLeft = Offset(x, y),
                        size = Size(cellSize, cellSize),
                        cornerRadius = cornerRadius
                    )
                    
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = cellSize * 0.08f,
                        center = Offset(x + cellSize / 2f, y + cellSize / 2f)
                    )
                }
            }

            // 2. Draw Hover Glow
            if (previewState != null) {
                val glowColor = Color.White.copy(alpha = 0.2f)
                for (r in previewState.clearedRows) {
                    val y = r * (cellSize + spacing)
                    drawRect(color = glowColor, topLeft = Offset(0f, y), size = Size(boardWidth, cellSize))
                }
                for (c in previewState.clearedCols) {
                    val x = c * (cellSize + spacing)
                    drawRect(color = glowColor, topLeft = Offset(x, 0f), size = Size(cellSize, boardHeight))
                }
            }

            // 3. Draw Occupied Cells
            for (r in 0 until 8) {
                for (c in 0 until 8) {
                    val colorId = grid[r][c]
                    if (colorId > 0) {
                        val x = c * (cellSize + spacing)
                        val y = r * (cellSize + spacing)
                        drawJewelBlock(x, y, cellSize, ShapeColors.getColorForId(colorId))
                    }
                }
            }

            // 4. Ghost
            if (previewState != null && ghostColor != null) {
                for ((r, c) in previewState.cells) {
                    if (r in 0..7 && c in 0..7) {
                        val x = c * (cellSize + spacing)
                        val y = r * (cellSize + spacing)
                        drawJewelBlock(x, y, cellSize, ghostColor.copy(alpha = ghostPulse))
                    }
                }
            }

            // 5. Shatter Animation (falling down smoothly)
            if (clearAlpha > 0.01f && lastClearedIndices.isNotEmpty()) {
                val clearedRows = lastClearedIndices.keys.groupBy { it.first }.filter { it.value.size == 8 }.keys
                val clearedCols = lastClearedIndices.keys.groupBy { it.second }.filter { it.value.size == 8 }.keys
                val isCombo = clearedRows.size + clearedCols.size >= 2

                val glowBrush = if (isCombo) {
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFFF3B30), Color(0xFFFF9500), Color(0xFFFFCC00), Color(0xFF4CD964), Color(0xFF5AC8FA), Color(0xFF5856D6)),
                        start = Offset(0f, 0f), end = Offset(boardWidth, boardHeight)
                    )
                } else {
                    Brush.horizontalGradient(colors = listOf(Color(0xFFFFD700), Color(0xFFFFF8E7), Color(0xFFFFD700)))
                }

                val flashAlpha = (clearAlpha - 0.5f).coerceAtLeast(0f) * 2f
                if (flashAlpha > 0f) {
                    clearedRows.forEach { r ->
                        val y = r * (cellSize + spacing)
                        drawRect(brush = glowBrush, topLeft = Offset(0f, y), size = Size(boardWidth, cellSize), alpha = flashAlpha)
                    }
                    clearedCols.forEach { c ->
                        val x = c * (cellSize + spacing)
                        drawRect(brush = glowBrush, topLeft = Offset(x, 0f), size = Size(cellSize, boardHeight), alpha = flashAlpha)
                    }
                }

                // Shatter Logic
                val progress = 1f - clearAlpha
                val gravityY = progress * progress * 800f // Fall down faster/smoothly

                lastClearedIndices.forEach { (coord, colorId) ->
                    val r = coord.first
                    val c = coord.second
                    val blockColor = ShapeColors.getColorForId(colorId)
                    val baseX = c * (cellSize + spacing)
                    val baseY = r * (cellSize + spacing)

                    for (i in 0..3) {
                        val isLeft = i % 2 == 0
                        val isTop = i < 2
                        val pieceSize = cellSize / 2f

                        val seed = r * 31 + c * 17 + i
                        // Slight horizontal spread
                        val spreadX = ((seed % 10) - 5) * 10f * progress
                        // NO UPWARD SHOOTING (-). Only falling down (+) and slightly outwards
                        val spreadY = ((seed % 5)) * 10f * progress + gravityY
                        val rot = ((seed % 360) * progress * 2f)

                        val startX = baseX + if (isLeft) 0f else pieceSize
                        val startY = baseY + if (isTop) 0f else pieceSize

                        val finalX = startX + spreadX
                        val finalY = startY + spreadY

                        withTransform({
                            translate(left = finalX + pieceSize / 2, top = finalY + pieceSize / 2)
                            rotate(rot)
                            scale(clearAlpha) // Shrink as they fall
                        }) {
                            drawRoundRect(
                                color = blockColor.copy(alpha = clearAlpha),
                                topLeft = Offset(-pieceSize / 2, -pieceSize / 2),
                                size = Size(pieceSize, pieceSize),
                                cornerRadius = CornerRadius(pieceSize * 0.15f)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun DrawScope.drawJewelBlock(
    x: Float,
    y: Float,
    cellSize: Float,
    color: Color
) {
    val radius = cellSize * 0.12f
    val cr = CornerRadius(radius, radius)

    drawRoundRect(
        color = Color.Black.copy(alpha = 0.35f * color.alpha),
        topLeft = Offset(x, y + cellSize * 0.08f),
        size = Size(cellSize, cellSize * 0.92f),
        cornerRadius = cr
    )

    drawRoundRect(
        color = color,
        topLeft = Offset(x, y),
        size = Size(cellSize, cellSize),
        cornerRadius = cr
    )
    
    val innerOffset = cellSize * 0.1f
    val innerSize = cellSize - innerOffset * 2f
    val innerCr = CornerRadius(radius * 0.8f, radius * 0.8f)
    
    val bevelBrush = Brush.linearGradient(
        colors = listOf(Color.White.copy(alpha = 0.5f * color.alpha), Color.Black.copy(alpha = 0.25f * color.alpha)),
        start = Offset(x, y),
        end = Offset(x + cellSize, y + cellSize)
    )
    val strokeWidth = cellSize * 0.08f
    drawRoundRect(
        brush = bevelBrush,
        topLeft = Offset(x + strokeWidth/2, y + strokeWidth/2),
        size = Size(cellSize - strokeWidth, cellSize - strokeWidth),
        cornerRadius = cr,
        style = Stroke(width = strokeWidth)
    )

    val innerBrush = Brush.linearGradient(
        colors = listOf(color.copy(alpha = 0.9f * color.alpha), color.copy(alpha = 0.7f * color.alpha)),
        start = Offset(x + innerOffset, y + innerOffset),
        end = Offset(x + cellSize - innerOffset, y + cellSize - innerOffset)
    )
    drawRoundRect(
        brush = innerBrush,
        topLeft = Offset(x + innerOffset, y + innerOffset),
        size = Size(innerSize, innerSize),
        cornerRadius = innerCr
    )

    drawRoundRect(
        color = Color.White.copy(alpha = 0.35f * color.alpha),
        topLeft = Offset(x + innerOffset * 1.5f, y + innerOffset * 1.5f),
        size = Size(innerSize - innerOffset, innerSize * 0.35f),
        cornerRadius = CornerRadius(innerCr.x, innerCr.y)
    )
}
INNER_EOF
chmod +x update_shatter_animation.sh
./update_shatter_animation.sh
