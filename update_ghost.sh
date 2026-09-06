sed -i '/    val clearAnim = remember(lastClearedIndices)/i\
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "ghostPulse")\
    val ghostPulse by infiniteTransition.animateFloat(\
        initialValue = 0.3f,\
        targetValue = 0.7f,\
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(\
            animation = androidx.compose.animation.core.tween(800, easing = androidx.compose.animation.core.FastOutSlowInEasing),\
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse\
        ),\
        label = "pulse"\
    )\
' app/src/main/java/com/example/ui/components/BoardComposable.kt

sed -i 's/color = ghostColor.copy(alpha = 0.5f)/color = ghostColor.copy(alpha = ghostPulse)/g' app/src/main/java/com/example/ui/components/BoardComposable.kt
