package com.smashblock.game.ui.components

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import java.util.Random

/**
 * Handles the "Golden Line Clear Effect" on an Android Canvas.
 * Draws a bright golden additive rectangle over the cleared row that flashes and fades out.
 * Spawns 10 small star-shaped particles that float upwards with random velocities.
 */
class GoldenLineClearAnimator {

    private data class StarParticle(
        var x: Float,
        var y: Float,
        var speedX: Float,
        var speedY: Float,
        var alpha: Float = 255f,
        val size: Float,
        var rotation: Float,
        val rotationSpeed: Float
    )

    private data class LineFlash(
        val rectLeft: Float,
        val rectTop: Float,
        val rectRight: Float,
        val rectBottom: Float,
        var alpha: Float = 255f
    )

    private val starParticles = mutableListOf<StarParticle>()
    private val lineFlashes = mutableListOf<LineFlash>()
    
    private val random = Random()

    // Golden additive glow paint
    private val flashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFFFDD00") // Bright Golden Yellow
        // Shadow layer provides the intense neon edge glow
        setShadowLayer(25f, 0f, 0f, Color.parseColor("#FFFF8800")) 
    }

    // Bright white/gold star paint
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        setShadowLayer(10f, 0f, 0f, Color.parseColor("#FFFFDD00"))
    }

    /**
     * Triggered when a full row (or column) is cleared.
     * Pass the bounding box (left, top, right, bottom) of the cleared line.
     */
    fun triggerLineClear(left: Float, top: Float, right: Float, bottom: Float) {
        // 1. Add the Golden Rectangle Flash
        lineFlashes.add(LineFlash(left, top, right, bottom))

        // 2. Spawn 10 small star-shaped particles floating upwards
        val width = right - left
        val centerY = top + (bottom - top) / 2f
        
        for (i in 0 until 10) {
            // Spawn randomly dispersed along the length of the line
            val startX = left + random.nextFloat() * width
            
            // Random upward velocities
            val speedX = -60f + random.nextFloat() * 120f // Slight horizontal drift
            val speedY = -150f - random.nextFloat() * 250f // Fast float upwards
            
            // Random sizes and rotations
            val size = 10f + random.nextFloat() * 15f
            val rotation = random.nextFloat() * 360f
            val rotationSpeed = -180f + random.nextFloat() * 360f // Spin while floating

            starParticles.add(
                StarParticle(
                    x = startX,
                    y = centerY,
                    speedX = speedX,
                    speedY = speedY,
                    size = size,
                    rotation = rotation,
                    rotationSpeed = rotationSpeed
                )
            )
        }
    }

    /**
     * Call inside the game loop to update math.
     * @param deltaTime time in seconds since last frame
     */
    fun update(deltaTime: Float) {
        // Update Line Flashes
        val flashIterator = lineFlashes.iterator()
        while (flashIterator.hasNext()) {
            val flash = flashIterator.next()
            // Flash fades out extremely fast (e.g., over 0.3 seconds)
            flash.alpha -= 255f * (deltaTime / 0.3f)
            if (flash.alpha <= 0f) {
                flashIterator.remove()
            }
        }

        // Update Star Particles
        val starIterator = starParticles.iterator()
        while (starIterator.hasNext()) {
            val star = starIterator.next()
            
            // Move up and rotate
            star.x += star.speedX * deltaTime
            star.y += star.speedY * deltaTime
            star.rotation += star.rotationSpeed * deltaTime
            
            // Fade out slower than the flash (e.g., over 1.0 second)
            star.alpha -= 255f * (deltaTime / 1.0f)
            
            if (star.alpha <= 0f) {
                starIterator.remove()
            }
        }
    }

    /**
     * Call inside onDraw(canvas: Canvas)
     */
    fun draw(canvas: Canvas) {
        // Draw Golden Flashes
        for (flash in lineFlashes) {
            flashPaint.alpha = flash.alpha.toInt().coerceIn(0, 255)
            canvas.drawRect(flash.rectLeft, flash.rectTop, flash.rectRight, flash.rectBottom, flashPaint)
        }

        // Draw Stars
        for (star in starParticles) {
            starPaint.alpha = star.alpha.toInt().coerceIn(0, 255)
            
            canvas.save()
            canvas.translate(star.x, star.y)
            canvas.rotate(star.rotation)
            drawStar(canvas, star.size, starPaint)
            canvas.restore()
        }
    }

    /**
     * Mathematical helper to draw a perfect 5-pointed star onto the canvas.
     */
    private fun drawStar(canvas: Canvas, radius: Float, paint: Paint) {
        val path = Path()
        val innerRadius = radius / 2.5f
        
        // Start at top point (0, -radius)
        path.moveTo(0f, -radius)
        
        // Connect 10 points (5 outer, 5 inner)
        for (i in 1 until 10) {
            val angle = i * Math.PI / 5f - Math.PI / 2f
            val r = if (i % 2 == 0) radius else innerRadius
            val x = (Math.cos(angle) * r).toFloat()
            val y = (Math.sin(angle) * r).toFloat()
            path.lineTo(x, y)
        }
        
        path.close()
        canvas.drawPath(path, paint)
    }
}
