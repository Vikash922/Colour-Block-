package com.smashblock.game.ui.components

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.animation.OvershootInterpolator

/**
 * Handles floating text animations on an Android Canvas (like "Combo 3!" or "+4").
 * The text spawns at specific X, Y coordinates, pops up with an overshoot/bounce effect,
 * slowly floats upwards, fades its alpha to 0 over 1 second, and then is destroyed.
 */
class FloatingTextAnimator {

    private data class FloatingText(
        val text: String,
        var x: Float,
        var y: Float,
        var scale: Float = 0f,
        var alpha: Int = 255,
        var isDead: Boolean = false
    )

    private val activeTexts = mutableListOf<FloatingText>()
    
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 80f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        // Nice drop shadow to make the text pop against the background
        setShadowLayer(8f, 0f, 4f, Color.parseColor("#99000000"))
    }

    fun spawnFloatingText(text: String, spawnX: Float, spawnY: Float) {
        val floatingText = FloatingText(text, spawnX, spawnY)
        activeTexts.add(floatingText)

        // 1. Pop up with an overshoot/bounce effect
        val scaleAnimator = ValueAnimator.ofFloat(0f, 1f)
        scaleAnimator.duration = 400
        scaleAnimator.interpolator = OvershootInterpolator(2f)
        scaleAnimator.addUpdateListener { anim ->
            floatingText.scale = anim.animatedValue as Float
        }

        // 2. Slowly float upwards and fade alpha to 0 over 1 second
        val fadeFloatAnimator = ValueAnimator.ofInt(255, 0)
        fadeFloatAnimator.startDelay = 600 // Wait 600ms at full opacity before fading
        fadeFloatAnimator.duration = 400   // Fade duration 400ms (Total lifespan = 1000ms / 1 second)
        fadeFloatAnimator.addUpdateListener { anim ->
            floatingText.alpha = anim.animatedValue as Int
            floatingText.y -= 3f // Float upwards by reducing Y on every tick
            
            // 3. Mark as destroyed when fully faded
            if (floatingText.alpha <= 0) {
                floatingText.isDead = true
            }
        }

        scaleAnimator.start()
        fadeFloatAnimator.start()
    }

    /**
     * Call this inside your Canvas View's onDraw() loop
     */
    fun draw(canvas: Canvas) {
        val iterator = activeTexts.iterator()
        while (iterator.hasNext()) {
            val ft = iterator.next()
            if (ft.isDead) {
                iterator.remove() // Destroyed completely from memory
                continue
            }

            paint.alpha = ft.alpha

            canvas.save()
            canvas.translate(ft.x, ft.y)
            canvas.scale(ft.scale, ft.scale)
            
            // Text is drawn centered at (0,0) of the translated canvas
            canvas.drawText(ft.text, 0f, 0f, paint)
            canvas.restore()
        }
    }
}
