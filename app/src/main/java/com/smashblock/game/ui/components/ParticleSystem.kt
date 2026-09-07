package com.smashblock.game.ui.components

import android.graphics.Canvas
import android.graphics.Paint
import java.util.Random

// Data class as requested
data class Particle(
    var x: Float, 
    var y: Float, 
    var speedX: Float, 
    var speedY: Float, 
    var color: Int, 
    var alpha: Int,
    var size: Float
)

class ParticleSystem {
    private val particles = mutableListOf<Particle>()
    private val random = Random()
    private val gravity = 900f // pixels per second squared
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * Call this when a block is destroyed.
     * Spawns 15 small square particles of the given color.
     */
    fun spawnParticles(startX: Float, startY: Float, color: Int) {
        for (i in 0 until 15) {
            // Outward radial velocity math
            val angle = random.nextDouble() * 2 * Math.PI
            val speed = 200f + random.nextFloat() * 400f // 200 to 600 px/s
            
            val speedX = (Math.cos(angle) * speed).toFloat()
            // Add a slight upward burst (-Y) so they pop up before falling
            val speedY = (Math.sin(angle) * speed).toFloat() - 300f 
            
            val size = 8f + random.nextFloat() * 12f // Random sizes between 8-20px

            particles.add(
                Particle(
                    x = startX,
                    y = startY,
                    speedX = speedX,
                    speedY = speedY,
                    color = color,
                    alpha = 255, // 255 is fully opaque in native Android Paint
                    size = size
                )
            )
        }
    }

    /**
     * Should be called in the rendering thread (e.g., inside SurfaceView's loop).
     * @param deltaTime The time passed since the last frame (in seconds).
     */
    fun update(deltaTime: Float) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            
            // Apply gravity to Y speed (accelerates downwards)
            p.speedY += gravity * deltaTime
            
            // Update X,Y positions
            p.x += p.speedX * deltaTime
            p.y += p.speedY * deltaTime
            
            // Fade alpha over time (lifespan of approx ~0.8 seconds)
            p.alpha -= (255 * deltaTime * 1.2f).toInt()
            
            // Delete particle if it's completely transparent
            if (p.alpha <= 0) {
                iterator.remove()
            }
        }
    }

    /**
     * Draws all active particles to the canvas.
     */
    fun draw(canvas: Canvas) {
        for (p in particles) {
            paint.color = p.color
            paint.alpha = p.alpha.coerceIn(0, 255)
            // Draw a small square particle
            canvas.drawRect(p.x, p.y, p.x + p.size, p.y + p.size, paint)
        }
    }
}
