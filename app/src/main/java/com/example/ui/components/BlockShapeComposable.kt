package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.BlockShape

@Composable
fun BlockShapeComposable(
    shape: BlockShape,
    cellSize: Dp,
    cellSpacing: Dp = 2.dp,
    modifier: Modifier = Modifier,
    alpha: Float = 1f
) {
    val totalWidth = cellSize * shape.cols + cellSpacing * (shape.cols - 1).coerceAtLeast(0)
    val totalHeight = cellSize * shape.rows + cellSpacing * (shape.rows - 1).coerceAtLeast(0)

    Box(
        modifier = modifier.size(width = totalWidth, height = totalHeight),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(width = totalWidth, height = totalHeight)) {
            val cellPx = cellSize.toPx()
            val spacePx = cellSpacing.toPx()

            for (r in 0 until shape.rows) {
                for (c in 0 until shape.cols) {
                    if (shape.matrix[r][c] != 0) {
                        val x = c * (cellPx + spacePx)
                        val y = r * (cellPx + spacePx)

                        drawDockJewelBlock(
                            x = x,
                            y = y,
                            cellSize = cellPx,
                            color = shape.color.copy(alpha = alpha)
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawDockJewelBlock(
    x: Float,
    y: Float,
    cellSize: Float,
    color: Color
) {
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
    drawPath(path = topPath, color = Color.White.copy(alpha = 0.3f * color.alpha))

    // Left highlight trapezoid (lighter)
    val leftPath = Path().apply {
        moveTo(x, y)
        lineTo(x + bevelWidth, y + bevelWidth)
        lineTo(x + bevelWidth, y + cellSize - bevelWidth)
        lineTo(x, y + cellSize)
        close()
    }
    drawPath(path = leftPath, color = Color.White.copy(alpha = 0.15f * color.alpha))

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
    drawPath(path = rightPath, color = Color.Black.copy(alpha = 0.15f * color.alpha))

    // Bottom shadow trapezoid (darker)
    val bottomPath = Path().apply {
        moveTo(x, y + cellSize)
        lineTo(x + bevelWidth, y + cellSize - bevelWidth)
        lineTo(x + cellSize - bevelWidth, y + cellSize - bevelWidth)
        lineTo(x + cellSize, y + cellSize)
        close()
    }
    drawPath(path = bottomPath, color = Color.Black.copy(alpha = 0.25f * color.alpha))
    
    // Inner square gets a subtle outline
    drawRect(
        color = Color.Black.copy(alpha = 0.1f * color.alpha),
        topLeft = Offset(x + bevelWidth, y + bevelWidth),
        size = Size(cellSize - 2 * bevelWidth, cellSize - 2 * bevelWidth)
    )
}
