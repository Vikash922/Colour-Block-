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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Whatshot
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
import com.example.ui.theme.ElectricGold
import com.example.ui.theme.HighScorePill
import com.example.ui.theme.HotPink
import com.example.ui.theme.SunsetOrange

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
            // High Score Pill or Daily Challenge Title
            if (isDailyChallenge) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFE53935), Color(0xFFF57C00))))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DAILY CHALLENGE",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier
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
            }

            // Settings Icon
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(48.dp).testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause/Settings",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Huge Score Text (Fixed height to prevent layout shifts)
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
            modifier = Modifier.height(84.dp).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = score.toString(),
                fontSize = 72.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                maxLines = 1,
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White, Color(0xFFB0BEC5))
                    )
                ),
                modifier = Modifier.scale(scoreAnimScale.value)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Combo Streak Badge
        Box(
            modifier = Modifier.height(44.dp),
            contentAlignment = Alignment.Center
        ) {
            this@Column.AnimatedVisibility(
                visible = comboCount > 0,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
            val infiniteTransition = rememberInfiniteTransition(label = "combo_pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "combo_scale"
            )

            Box(
                modifier = Modifier
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(SunsetOrange, HotPink)))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("combo_badge"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Whatshot,
                        contentDescription = "Combo Blast",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${comboCount}x",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
        }
    }
}

