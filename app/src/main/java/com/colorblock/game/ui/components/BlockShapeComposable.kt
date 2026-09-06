package com.colorblock.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.colorblock.game.model.BlockShape

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
    val radius = cellSize * 0.12f
    val cr = CornerRadius(radius, radius)

    // 1. Shadow/Base at the bottom (giving it height/thickness)
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.35f * color.alpha),
        topLeft = Offset(x, y + cellSize * 0.08f),
        size = Size(cellSize, cellSize * 0.92f),
        cornerRadius = cr
    )

    // 2. Main base color
    drawRoundRect(
        color = color,
        topLeft = Offset(x, y),
        size = Size(cellSize, cellSize),
        cornerRadius = cr
    )
    
    val innerOffset = cellSize * 0.1f
    val innerSize = cellSize - innerOffset * 2f
    val innerCr = CornerRadius(radius * 0.8f, radius * 0.8f)
    
    // 3. Bevel stroke gradient (White top-left, Black bottom-right)
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

    // 4. Inner surface gradient (lighter to darker)
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

    // 5. Specular highlight curve on the top inner surface
    drawRoundRect(
        color = Color.White.copy(alpha = 0.35f * color.alpha),
        topLeft = Offset(x + innerOffset * 1.5f, y + innerOffset * 1.5f),
        size = Size(innerSize - innerOffset, innerSize * 0.35f),
        cornerRadius = CornerRadius(innerCr.x, innerCr.y)
    )
}
