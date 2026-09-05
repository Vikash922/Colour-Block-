package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HighScorePill

@Composable
fun HeaderComposable(
    score: Int,
    highScore: Int,
    comboCount: Int,
    isSoundEnabled: Boolean,
    onToggleSound: () -> Unit,
    onRestartGame: () -> Unit,
    onSettingsClick: () -> Unit = {},
    isDailyChallenge: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Action Bar: Trophy pill and Settings
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // High Score Pill
            Row(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(HighScorePill)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Trophy",
                    tint = Color(0xFFFFD700), // Gold
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = highScore.toString(),
                    color = Color(0xFFFFD700),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Settings Icon
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.padding(end = 16.dp).size(48.dp).testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Pause/Settings",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Huge Score Text with Heart Background
        val scoreAnimScale = androidx.compose.runtime.remember(score) { androidx.compose.animation.core.Animatable(if (score > 0) 1.2f else 1f) }
        androidx.compose.runtime.LaunchedEffect(score) {
            if (score > 0) {
                scoreAnimScale.animateTo(
                    targetValue = 1f,
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 300, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                )
            }
        }

        Box(
            modifier = Modifier.height(100.dp).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Heart Icon Behind (Only when score > 500)
            if (score > 500) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFFF4081),
                    modifier = Modifier.size(140.dp)
                )
            }

            Text(
                text = score.toString(),
                fontSize = 72.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                maxLines = 1,
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White, Color(0xFFE0E0E0))
                    )
                ),
                modifier = Modifier.scale(scoreAnimScale.value)
            )
        }
    }
}
