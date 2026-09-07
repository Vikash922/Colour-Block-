package com.smashblock.game.ui.components

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.view.animation.OvershootInterpolator

/**
 * 1. Bouncy UI (Pop-ups)
 * Handles popping up texts like "Combo!" or "Good!" with a bouncy Overshoot effect.
 */
class ComboPopupManager {
    
    data class PopupText(
        var text: String,
        var x: Float,
        var y: Float,
        var scale: Float = 0f,
        var alpha: Int = 255,
        var isAlive: Boolean = true
    )
    
    private val popups = mutableListOf<PopupText>()
    
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 70f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        // Add a slight dark shadow behind the text so it stands out
        setShadowLayer(10f, 0f, 5f, Color.parseColor("#80000000"))
    }

    fun spawnPopup(text: String, startX: Float, startY: Float) {
        val popup = PopupText(text, startX, startY)
        popups.add(popup)
        
        // Bouncy UI: ValueAnimator with OvershootInterpolator(2f)
        val bounceAnimator = ValueAnimator.ofFloat(0f, 1.2f, 1f)
        bounceAnimator.duration = 500
        bounceAnimator.interpolator = OvershootInterpolator(2f)
        bounceAnimator.addUpdateListener { anim ->
            popup.scale = anim.animatedValue as Float
        }
        
        // Float up and fade out after a short delay
        val fadeAnimator = ValueAnimator.ofInt(255, 0)
        fadeAnimator.startDelay = 700
        fadeAnimator.duration = 400
        fadeAnimator.addUpdateListener { anim ->
            popup.alpha = anim.animatedValue as Int
            popup.y -= 2f // Float upwards
            if (popup.alpha == 0) popup.isAlive = false
        }
        
        bounceAnimator.start()
        fadeAnimator.start()
    }

    fun draw(canvas: Canvas) {
        val iterator = popups.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            if (!p.isAlive) {
                iterator.remove()
                continue
            }
            
            textPaint.alpha = p.alpha
            
            canvas.save()
            canvas.translate(p.x, p.y)
            // Scale the canvas to create the zoom-in bounce effect
            canvas.scale(p.scale, p.scale)
            canvas.drawText(p.text, 0f, 0f, textPaint)
            canvas.restore()
        }
    }
}

/**
 * 2 & 3. Rainbow Line Clear Glow & Combo Border
 * Handles neon shadow glows and animated rainbow borders using Paint.shader and Matrix.
 */
class RainbowGlowRenderer {
    
    // Neon Glow Paint using setShadowLayer
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        // Additive glow: large radius behind the object
        setShadowLayer(35f, 0f, 0f, Color.parseColor("#FF00FF")) 
    }
    
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }
    
    private val gradientMatrix = Matrix()
    
    // Rainbow colors for the LinearGradient
    private val rainbowColors = intArrayOf(
        Color.RED, 
        Color.parseColor("#FFA500"), // Orange
        Color.YELLOW,
        Color.GREEN, 
        Color.BLUE, 
        Color.parseColor("#4B0082"), // Indigo
        Color.parseColor("#EE82EE")  // Violet
    )
    
    // Create the LinearGradient shader
    private val shader = LinearGradient(
        0f, 0f, 300f, 300f, 
        rainbowColors, 
        null, 
        Shader.TileMode.MIRROR
    )

    init {
        // Assign shader to the border paint
        borderPaint.shader = shader
    }

    /**
     * Draws a block with a massive neon shadow glow.
     * Perfect for when a line is just about to clear.
     */
    fun drawNeonGlowBlock(canvas: Canvas, rect: RectF, blockColor: Int, neonGlowColor: Int) {
        glowPaint.color = blockColor
        // Update shadow color dynamically based on line/block color
        glowPaint.setShadowLayer(35f, 0f, 0f, neonGlowColor)
        
        canvas.drawRoundRect(rect, 15f, 15f, glowPaint)
    }

    /**
     * Draws an animated rainbow border.
     * Call this inside a render loop and pass System.currentTimeMillis()
     */
    fun drawAnimatedRainbowBorder(canvas: Canvas, rect: RectF, timeMillis: Long) {
        // Animate the rotation of the gradient Matrix
        val rotation = (timeMillis % 4000L) / (4000L / 360f) // Rotates 360 degrees every 4s
        
        gradientMatrix.reset()
        gradientMatrix.setRotate(rotation, rect.centerX(), rect.centerY())
        
        // Apply the rotated matrix to the shader
        shader.setLocalMatrix(gradientMatrix)
        
        canvas.drawRoundRect(rect, 20f, 20f, borderPaint)
    }
}
