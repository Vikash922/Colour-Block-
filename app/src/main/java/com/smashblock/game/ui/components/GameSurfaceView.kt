package com.smashblock.game.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.ContextCompat

class GameSurfaceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback, Runnable {

    private var thread: Thread? = null
    @Volatile
    private var isPlaying = false

    // 10x10 Grid representation
    private val gridSize = 10
    private var grid = Array(gridSize) { IntArray(gridSize) }

    // Drawing variables
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#33FFFFFF")
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    
    private var cellWidth: Float = 0f
    private var cellHeight: Float = 0f
    
    // Touch state
    private var touchedRow = -1
    private var touchedCol = -1
    private var isDragging = false

    // Sprites (Placeholders for PNG/WEBP)
    private val blockSprites = mutableMapOf<Int, Bitmap>()

    init {
        holder.addCallback(this)
        setZOrderOnTop(false)
        // Initialization of Sprites would happen here
        // e.g., blockSprites[1] = BitmapFactory.decodeResource(resources, R.drawable.block_red)
        // For now, we will dynamically generate glossy bitmaps in memory to act as sprites
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isPlaying = true
        thread = Thread(this).apply { start() }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Calculate cell sizes for 10x10 grid
        val boardSize = Math.min(width, height) * 0.95f
        cellWidth = boardSize / gridSize
        cellHeight = boardSize / gridSize
        
        // Generate placeholder glossy sprites
        generatePlaceholderSprites(cellWidth.toInt())
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        isPlaying = false
        while (retry) {
            try {
                thread?.join()
                retry = false
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    override fun run() {
        val targetFPS = 60
        val targetTime = 1000L / targetFPS

        while (isPlaying) {
            val startTime = System.currentTimeMillis()
            
            if (holder.surface.isValid) {
                val canvas = holder.lockCanvas()
                if (canvas != null) {
                    synchronized(holder) {
                        drawGame(canvas)
                    }
                    holder.unlockCanvasAndPost(canvas)
                }
            }
            
            val timeMillis = System.currentTimeMillis() - startTime
            val waitTime = targetTime - timeMillis
            if (waitTime > 0) {
                try {
                    Thread.sleep(waitTime)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun drawGame(canvas: Canvas) {
        // Clear background
        canvas.drawColor(Color.parseColor("#1A1F3D"))

        val startX = (width - (cellWidth * gridSize)) / 2f
        val startY = (height - (cellHeight * gridSize)) / 2f

        // Draw Grid and Blocks
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val x = startX + col * cellWidth
                val y = startY + row * cellHeight

                // Draw empty cell border
                canvas.drawRect(x, y, x + cellWidth, y + cellHeight, gridPaint)

                // Draw Block Sprite if occupied
                val blockId = grid[row][col]
                if (blockId > 0 && blockSprites.containsKey(blockId)) {
                    val sprite = blockSprites[blockId]
                    if (sprite != null) {
                        canvas.drawBitmap(sprite, x, y, null)
                    }
                }

                // Highlight cell if being dragged over
                if (isDragging && row == touchedRow && col == touchedCol) {
                    paint.color = Color.parseColor("#66FFFFFF")
                    canvas.drawRect(x, y, x + cellWidth, y + cellHeight, paint)
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val startX = (width - (cellWidth * gridSize)) / 2f
        val startY = (height - (cellHeight * gridSize)) / 2f

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                isDragging = true
                val touchX = event.x
                val touchY = event.y

                // Calculate which grid cell the user is dragging their finger over
                if (touchX >= startX && touchX <= startX + cellWidth * gridSize &&
                    touchY >= startY && touchY <= startY + cellHeight * gridSize) {
                    
                    touchedCol = ((touchX - startX) / cellWidth).toInt()
                    touchedRow = ((touchY - startY) / cellHeight).toInt()
                    
                    // Constrain bounds just in case
                    touchedCol = touchedCol.coerceIn(0, gridSize - 1)
                    touchedRow = touchedRow.coerceIn(0, gridSize - 1)
                } else {
                    touchedRow = -1
                    touchedCol = -1
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                
                // Example: Place a block if they released over a valid cell
                if (touchedRow in 0 until gridSize && touchedCol in 0 until gridSize) {
                    grid[touchedRow][touchedCol] = (1..5).random() // Random color block
                }
                
                touchedRow = -1
                touchedCol = -1
            }
        }
        return true
    }

    // This simulates loading PNG/WEBP sprites by generating bitmap sprites in memory
    private fun generatePlaceholderSprites(size: Int) {
        if (size <= 0) return
        val colors = listOf(
            Color.parseColor("#FF3B30"), // Red
            Color.parseColor("#4CD964"), // Green
            Color.parseColor("#5AC8FA"), // Blue
            Color.parseColor("#FFCC00"), // Yellow
            Color.parseColor("#AF52DE")  // Purple
        )
        
        for ((index, color) in colors.withIndex()) {
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // Draw Base
            paint.color = color
            canvas.drawRoundRect(0f, 0f, size.toFloat(), size.toFloat(), size * 0.15f, size * 0.15f, paint)
            
            // Draw Bevel Highlights (Top-Left Light, Bottom-Right Dark)
            val bevelSize = size * 0.15f
            val path = Path()
            
            // Top highlight
            paint.color = Color.parseColor("#4DFFFFFF") // 30% White
            path.moveTo(0f, 0f)
            path.lineTo(size.toFloat(), 0f)
            path.lineTo(size - bevelSize, bevelSize)
            path.lineTo(bevelSize, bevelSize)
            path.close()
            canvas.drawPath(path, paint)
            
            // Bottom shadow
            paint.color = Color.parseColor("#40000000") // 25% Black
            path.reset()
            path.moveTo(0f, size.toFloat())
            path.lineTo(bevelSize, size - bevelSize)
            path.lineTo(size - bevelSize, size - bevelSize)
            path.lineTo(size.toFloat(), size.toFloat())
            path.close()
            canvas.drawPath(path, paint)
            
            blockSprites[index + 1] = bitmap
        }
    }
}
