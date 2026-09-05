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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import com.example.model.ShapeColors
import com.example.ui.theme.GridBackground
import com.example.ui.theme.GridLine
import com.example.viewmodel.PreviewState

@Composable
fun BoardComposable(
    grid: Array<IntArray>,
    previewState: PreviewState?,
    ghostColor: Color?,
    lastClearedIndices: Set<Pair<Int, Int>>,
    onBoardGloballyPositioned: (LayoutCoordinates) -> Unit,
    modifier: Modifier = Modifier
) {
    // Animation for line clear flash
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
            .background(GridLine) // The thin line color is the background
            .onGloballyPositioned { coordinates ->
                onBoardGloballyPositioned(coordinates)
            }
            .testTag("game_board")
    ) {
        val clearAlpha = clearAnim.value

        Canvas(modifier = Modifier.fillMaxSize()) {
            val boardWidth = size.width
            val boardHeight = size.height
            val spacing = 2f // Thinner spacing for modern flat look
            val totalSpacing = spacing * 7 // 7 inner gaps
            val cellSize = (boardWidth - totalSpacing) / 8f
            // Minimal corner radius if any, let's keep it very slight
            val cornerRadius = CornerRadius(0f, 0f)

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
                }
            }

            // 2. Draw Hover Glow for lines that WILL be cleared
            if (previewState != null) {
                val glowColor = Color.White.copy(alpha = 0.2f)
                for (r in previewState.clearedRows) {
                    val y = r * (cellSize + spacing)
                    drawRect(
                        color = glowColor,
                        topLeft = Offset(0f, y),
                        size = Size(boardWidth, cellSize)
                    )
                }
                for (c in previewState.clearedCols) {
                    val x = c * (cellSize + spacing)
                    drawRect(
                        color = glowColor,
                        topLeft = Offset(x, 0f),
                        size = Size(cellSize, boardHeight)
                    )
                }
            }

            // 3. Draw Occupied Cells
            for (r in 0 until 8) {
                for (c in 0 until 8) {
                    val colorId = grid[r][c]
                    if (colorId > 0) {
                        val x = c * (cellSize + spacing)
                        val y = r * (cellSize + spacing)
                        val blockColor = ShapeColors.getColorForId(colorId)

                        drawJewelBlock(
                            x = x,
                            y = y,
                            cellSize = cellSize,
                            color = blockColor
                        )
                    }
                }
            }

            // 4. Draw Ghost Preview Cells
            if (previewState != null && ghostColor != null) {
                for ((r, c) in previewState.cells) {
                    if (r in 0..7 && c in 0..7) {
                        val x = c * (cellSize + spacing)
                        val y = r * (cellSize + spacing)

                        drawJewelBlock(
                            x = x,
                            y = y,
                            cellSize = cellSize,
                            color = ghostColor.copy(alpha = ghostPulse)
                        )
                    }
                }
            }

            // 5. Draw Cleared Line Burst & Shatter Animation
            if (clearAlpha > 0.01f && lastClearedIndices.isNotEmpty()) {
                val clearedRows = lastClearedIndices.groupBy { it.first }.filter { it.value.size == 8 }.keys
                val clearedCols = lastClearedIndices.groupBy { it.second }.filter { it.value.size == 8 }.keys

                val isCombo = clearedRows.size + clearedCols.size >= 2

                val glowBrush = if (isCombo) {
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFFF3B30), Color(0xFFFF9500), Color(0xFFFFCC00), Color(0xFF4CD964), Color(0xFF5AC8FA), Color(0xFF5856D6)),
                        start = Offset(0f, 0f), end = Offset(boardWidth, boardHeight)
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFFFFD700), Color(0xFFFFF8E7), Color(0xFFFFD700))
                    )
                }

                // Draw initial bright flash lines that fade fast
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

                // Draw Shattering Particles
                val progress = 1f - clearAlpha
                val gravityY = progress * progress * 400f

                lastClearedIndices.forEach { (r, c) ->
                    val baseX = c * (cellSize + spacing)
                    val baseY = r * (cellSize + spacing)

                    for (i in 0..3) {
                        val isLeft = i % 2 == 0
                        val isTop = i < 2
                        val pieceSize = cellSize / 2f

                        val seed = r * 31 + c * 17 + i
                        val spreadX = ((seed % 10) - 5) * 12f * progress
                        val spreadY = -((seed % 15) + 5) * 10f * progress + gravityY
                        val rot = ((seed % 360) * progress * 2f)

                        val startX = baseX + if (isLeft) 0f else pieceSize
                        val startY = baseY + if (isTop) 0f else pieceSize

                        val finalX = startX + spreadX
                        val finalY = startY + spreadY

                        withTransform({
                            translate(left = finalX + pieceSize / 2, top = finalY + pieceSize / 2)
                            rotate(rot)
                        }) {
                            drawRect(
                                color = Color.White.copy(alpha = clearAlpha),
                                topLeft = Offset(-pieceSize / 2, -pieceSize / 2),
                                size = Size(pieceSize, pieceSize)
                            )
                        }
                    }
                }
            }
            }
        }
    }

/**
 * Draws a jewel block mimicking the reference image bevel style.
 */
private fun DrawScope.drawJewelBlock(
    x: Float,
    y: Float,
    cellSize: Float,
    color: Color
) {
    // The reference image blocks have a distinct inner bevel.
    // Base color
    drawRect(
        color = color,
        topLeft = Offset(x, y),
        size = Size(cellSize, cellSize)
    )

    // Bevel highlights
    val bevelWidth = cellSize * 0.15f
    
    // Top highlight trapezoid (lighter)
    val topPath = Path().apply {
        moveTo(x, y)
        lineTo(x + cellSize, y)
        lineTo(x + cellSize - bevelWidth, y + bevelWidth)
        lineTo(x + bevelWidth, y + bevelWidth)
        close()
    }
    drawPath(path = topPath, color = Color.White.copy(alpha = 0.3f))

    // Left highlight trapezoid (lighter)
    val leftPath = Path().apply {
        moveTo(x, y)
        lineTo(x + bevelWidth, y + bevelWidth)
        lineTo(x + bevelWidth, y + cellSize - bevelWidth)
        lineTo(x, y + cellSize)
        close()
    }
    drawPath(path = leftPath, color = Color.White.copy(alpha = 0.15f))

    // Inner diagonal reflection shine
    val shinePath = Path().apply {
        moveTo(x + bevelWidth, y + bevelWidth)
        lineTo(x + cellSize * 0.6f, y + bevelWidth)
        lineTo(x + bevelWidth, y + cellSize * 0.6f)
        close()
    }
    drawPath(path = shinePath, color = Color.White.copy(alpha = 0.2f * color.alpha))

    // Right shadow trapezoid (darker)
    val rightPath = Path().apply {
        moveTo(x + cellSize, y)
        lineTo(x + cellSize, y + cellSize)
        lineTo(x + cellSize - bevelWidth, y + cellSize - bevelWidth)
        lineTo(x + cellSize - bevelWidth, y + bevelWidth)
        close()
    }
    drawPath(path = rightPath, color = Color.Black.copy(alpha = 0.15f))

    // Bottom shadow trapezoid (darker)
    val bottomPath = Path().apply {
        moveTo(x, y + cellSize)
        lineTo(x + bevelWidth, y + cellSize - bevelWidth)
        lineTo(x + cellSize - bevelWidth, y + cellSize - bevelWidth)
        lineTo(x + cellSize, y + cellSize)
        close()
    }
    drawPath(path = bottomPath, color = Color.Black.copy(alpha = 0.25f))
    
    // Inner square gets a subtle outline
    drawRect(
        color = Color.Black.copy(alpha = 0.1f),
        topLeft = Offset(x + bevelWidth, y + bevelWidth),
        size = Size(cellSize - 2 * bevelWidth, cellSize - 2 * bevelWidth)
    )
}
