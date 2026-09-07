package com.smashblock.game.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import kotlin.math.roundToInt

/**
 * Custom Android Canvas View demonstrating Polyomino dragging, 
 * nearest grid alignment calculation, semi-transparent "ghost" shadow, 
 * and ACTION_UP snapping.
 */
class DragSnapCanvasView(context: Context) : View(context) {
    private val gridSize = 10
    private var grid = Array(gridSize) { IntArray(gridSize) }
    
    // Define a polyomino shape (e.g., an L-shape)
    private val shapeMatrix = arrayOf(
        intArrayOf(1, 0),
        intArrayOf(1, 0),
        intArrayOf(1, 1)
    )
    private val shapeRows = shapeMatrix.size
    private val shapeCols = shapeMatrix[0].size
    private val shapeColor = Color.parseColor("#5AC8FA") // Blue

    private var cellSize = 0f
    
    // Dragging state variables
    private var isDragging = false
    private var dragX = 0f
    private var dragY = 0f
    private var offsetX = 0f
    private var offsetY = 0f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // Calculate cell size based on screen width
        cellSize = Math.min(w, h) / gridSize.toFloat()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Pick up the shape
                isDragging = true
                dragX = touchX
                dragY = touchY
                
                // Set offset so the shape centers on the finger
                offsetX = (shapeCols * cellSize) / 2f
                offsetY = (shapeRows * cellSize) / 2f
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    dragX = touchX
                    dragY = touchY
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    isDragging = false
                    
                    // Nearest Grid Alignment Calculation
                    val targetCol = ((dragX - offsetX) / cellSize).roundToInt()
                    val targetRow = ((dragY - offsetY) / cellSize).roundToInt()
                    
                    // Snap Logic
                    if (canPlaceShape(targetRow, targetCol)) {
                        placeShape(targetRow, targetCol)
                    }
                    invalidate()
                }
            }
        }
        return true
    }

    private fun canPlaceShape(row: Int, col: Int): Boolean {
        // Check bounds
        if (row < 0 || col < 0 || row + shapeRows > gridSize || col + shapeCols > gridSize) return false
        
        // Check collisions
        for (r in 0 until shapeRows) {
            for (c in 0 until shapeCols) {
                if (shapeMatrix[r][c] != 0 && grid[row + r][col + c] != 0) {
                    return false
                }
            }
        }
        return true
    }

    private fun placeShape(row: Int, col: Int) {
        for (r in 0 until shapeRows) {
            for (c in 0 until shapeCols) {
                if (shapeMatrix[r][c] != 0) {
                    grid[row + r][col + c] = shapeColor
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.parseColor("#1A1F3D")) // Dark background

        // 1. Draw Grid and Placed Blocks
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                val cx = c * cellSize
                val cy = r * cellSize
                
                // Draw grid lines
                paint.style = Paint.Style.STROKE
                paint.color = Color.parseColor("#33FFFFFF")
                canvas.drawRect(cx, cy, cx + cellSize, cy + cellSize, paint)
                
                // Draw placed blocks
                val cellColor = grid[r][c]
                if (cellColor != 0) {
                    paint.style = Paint.Style.FILL
                    paint.color = cellColor
                    canvas.drawRect(cx + 2f, cy + 2f, cx + cellSize - 2f, cy + cellSize - 2f, paint)
                }
            }
        }

        // 2. Draw Ghost Shadow and Dragged Shape
        if (isDragging) {
            // Calculate nearest landing spot
            val targetCol = ((dragX - offsetX) / cellSize).roundToInt()
            val targetRow = ((dragY - offsetY) / cellSize).roundToInt()

            // Draw Semi-transparent Ghost Shadow
            if (canPlaceShape(targetRow, targetCol)) {
                paint.style = Paint.Style.FILL
                paint.color = shapeColor
                paint.alpha = 80 // Semi-transparent
                
                for (r in 0 until shapeRows) {
                    for (c in 0 until shapeCols) {
                        if (shapeMatrix[r][c] != 0) {
                            val gx = (targetCol + c) * cellSize
                            val gy = (targetRow + r) * cellSize
                            canvas.drawRect(gx + 2f, gy + 2f, gx + cellSize - 2f, gy + cellSize - 2f, paint)
                        }
                    }
                }
            }

            // Draw Opaque Dragged Shape tracking the finger
            paint.style = Paint.Style.FILL
            paint.color = shapeColor
            paint.alpha = 255 // Solid
            
            val shapeDrawX = dragX - offsetX
            val shapeDrawY = dragY - offsetY
            
            for (r in 0 until shapeRows) {
                for (c in 0 until shapeCols) {
                    if (shapeMatrix[r][c] != 0) {
                        val px = shapeDrawX + c * cellSize
                        val py = shapeDrawY + r * cellSize
                        canvas.drawRect(px + 2f, py + 2f, px + cellSize - 2f, py + cellSize - 2f, paint)
                    }
                }
            }
        }
    }
}
